package com.cloudIaas.cloudIaas.container;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

@Slf4j
@Component
public class ContainerCommandExecutor {

    //외부 명령(cmd)을 실행하고, 정상 종료(exit code 0)하면 true, 아니면 false를 반환하는 메소드
    public boolean run(String... cmd) { // 파라미터로 cmd 실행할 명령어와 인자들 받음
        try {
            // ProcessBuilder는 외부 OS 명령을 실행하는 Java 내장 도구
            // cmd 배열에 들어있는 명령어로 프로세스를 구성한다.
            ProcessBuilder pb = new ProcessBuilder(cmd);

            // start()를 호출하면 실제 OS 레벨에서 프로세스가 실행됨
            Process p = pb.start();

            // waitFor()는 외부 프로세스가 끝날 때까지 대기하는 메소드
            // 프로세스가 종료되면 exit code(종료 코드)를 반환함
            int code = p.waitFor();

            // 일반적으로 종료 코드가 0이면 정상 종료를 의미함
            return code == 0;
        } catch (Exception e) {
            // 외부 명령 실행 중 예외 발생 시 false
            return false;
        }
    }

    //외부 명령(cmd)을 실행한 후, 그 표준 출력(stdout)을 문자열로 읽어서 반환하는 메소드
    public String runAndRead(String... cmd) {
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            Process p = pb.start();

            // 표준 출력 스트림을 읽어오기 위한 StringBuilder
            StringBuilder sb = new StringBuilder();

            // 프로세스의 표준 출력(InputStream)을 BufferedReader로 감싸서 한 줄씩 읽기
            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = r.readLine()) != null) {
                    // 한 줄씩 sb에 추가 (뒤에 개행도 붙임)
                    sb.append(line).append("\n");
                }
            }
            // 프로세스 종료까지 대기 (출력이 모두 읽혔는지 보장)
            p.waitFor();

            // 마지막 개행 제거 후 문자열 반환
            return sb.toString().trim();
        } catch (Exception e) {
            // 예외 발생 시 null 반환
            return null;
        }
    }
}