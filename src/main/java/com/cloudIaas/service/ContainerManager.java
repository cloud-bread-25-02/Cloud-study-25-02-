// java
package com.cloudIaas.service;

import com.cloudIaas.container.ContainerCommandExecutor;
import com.cloudIaas.domain.UserInfo;
import com.cloudIaas.model.ContainerInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


// 컨테이너 관련 비즈니스 로직
// 실제 도커 명령 실행 및 상태 처리 담당
@Slf4j
@RequiredArgsConstructor
@Service
public class ContainerManager {

    private final ContainerCommandExecutor executor;

    @Value("${container.jupyter.port:8888}")
    private int jupyterPort; // 주피터 내부 포트 번호

    @Value("${container.jupyter.image:jupyter/base-notebook}")
    private String jupyterImage;

    // 새로운 컨테이너 생성 후 바로 접속
    // 새 컨테이너를 생성하고, 접속 가능한 URL을 반환하는 메소드
    public String createContainer(UserInfo user, String serverDomain) {

        // 소유자 ID 추출 (없으면 unknown)
        String owner = (user == null) ? "unknown" : user.getUserId();

        // 컨테이너 이름: ct-사용자ID-타임스탬프
        String containerName = "ct-" + owner + "-" + System.currentTimeMillis();

        boolean ok = executor.run("docker", "run",
                "-d", // 백그라운드 실행
                "--name", containerName,
                "--label", "userId=" + owner, // 컨테이너에 사용자 ID 라벨 추가
                "-P",  // 컨테이너 포트를 호스트에 자동 매핑
                jupyterImage
        );

        // 실행 실패 시 null 반환 + 로그 출력
        if (!ok) {
            log.error("Failed to run container {} from image {}", containerName, jupyterImage);
            return null;
        }
        // 성공 시 자동 생성된 포트를 기반으로 접속 URL 생성
        return getContainerUrl(containerName);
    }

    // 실행 중인 컨테이너에 접속
    // 컨테이너의 외부 접속 URL을 반환하는 메소드 (예: http://localhost:32768)
    public String getContainerUrl(String containerName) {

        // 최대 10번(5초)까지 polling 하면서 포트 매핑 정보(docker port)를 가져옴
        String portMapping = null;
        for (int i = 0; i < 10; i++) {
            portMapping = executor.runAndRead("docker", "port", containerName, jupyterPort + "/tcp");

            // 결과가 정상적으로 나오면 반복 종료
            if (portMapping != null && !portMapping.isBlank()) break;

            // 아직 매핑이 생성되지 않으면 0.5초 대기 후 재시도
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }

        // 끝까지 포트를 못 찾으면 오류 반환
        if (portMapping == null || portMapping.isBlank()) {
            log.error("No port mapping for container {} port {}", containerName, jupyterPort);
            return null;
        }

        // docker port 출력 예: "0.0.0.0:32770" 또는 ":::32770"
        // 마지막 ':' 뒤의 숫자가 실제 hostPort
        String hostPort = portMapping.contains(":")
                ? portMapping.substring(portMapping.lastIndexOf(':') + 1).trim()
                : portMapping.trim();


        // 외부 접근 URL 생성 (현재는 localhost 기준)
        return "http://localhost:" + hostPort;
    }


    // 로그인 사용자 접두사 기준으로 컨테이너 목록 조회
    public List<ContainerInfo> listUserContainers(String prefix) {
        // prefix가 null이거나 빈 문자열이면 조회할 수 없으므로 빈 리스트 반환
        if (prefix == null || prefix.isEmpty()) return List.of();

        String out = executor.runAndRead(
                "docker", "ps",
                        "-a", // 종료된 컨테이너 포함 전체 컨테이너 조회
                        "--filter", "name=" + prefix, // 이름에 prefix가 포함된 컨테이너만 필터링
                        "--format", "{{.Names}}||{{.Status}}" // 출력 형식을 “컨테이너이름||컨테이너상태” 형태로 지정
        );

        // 명령 실행 실패(out == null) 또는 출력 없음(out.isBlank()) → 빈 리스트 반환
        if (out == null || out.isBlank()) return List.of();

        // 컨테이너는 줄 단위로 분리되어 있으므로 개행 기준 split
        String[] lines = out.split("\\r?\\n");
        List<ContainerInfo> list = new ArrayList<>();

        // 각 줄을 순회하며 컨테이너 정보 객체로 변환
        for (String line : lines) {

            // 한 줄의 형식: "컨테이너명||상태"
            // "||" 를 기준으로 최대 2개로 split 함
            String[] p = line.split("\\|\\|", 2);

            // p[0] → 컨테이너 이름(ct-사용자ID-타임스탬프)
            String name = p.length > 0 ? p[0].trim() : "";
            // p[1] → 컨테이너 상태 (없으면 공백)
            String status = p.length > 1 ? p[1].trim() : "";

            // Up으로 시작하면 running = true
            boolean running = status.startsWith("Up");

            list.add(new ContainerInfo(name, status, running, null));
        }
        // 최종적으로 컨테이너 목록 반환
        return list;
    }

    // 중지된 컨테이너 재시작
    public boolean startContainer(String containerName) {
        return executor.run("docker", "start", containerName);
    }

    // (기존) 컨테이너 중지
    public boolean stopContainer(String containerName) {
        return executor.run("docker", "stop", containerName);
    }

    // (기존) 컨테이너 삭제
    public boolean deleteContainer(String containerName) {
        return executor.run("docker", "rm", "-f", containerName);
    }

    // 컨테이너 존재 여부 확인
    public boolean containerExists(String prefix, String containerName) {
        String out = executor.runAndRead("docker", "ps", "-a", "--filter", "name=" + prefix, "--format", "{{.Names}}");
        if (out == null || out.isBlank()) return false;

        for (String line : out.split("\\r?\\n")) {
            if (line.trim().equals(containerName)) return true;
        }
        return false;
    }
    
    // 컨테이너 실행 여부 확인
    public boolean isRunning(String containerName) {
        String out = executor.runAndRead("docker", "inspect", "-f", "{{.State.Running}}", containerName);
        return out != null && out.trim().equalsIgnoreCase("true");
    }

}