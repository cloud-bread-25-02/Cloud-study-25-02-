package com.cloudIaas.controller;

import com.cloudIaas.domain.UserInfo;
import com.cloudIaas.jwt_util.AuthService;
import com.cloudIaas.service.ContainerManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/container")
public class ContainerController {

    private final ContainerManager containerManager;
    private final AuthService authService; // JWT 기반 로그인 사용자 정보 조회
    private final String serverDomain = "localhost";

    // 1. GET /container/list
    // 로그인한 사용자의 컨테이너 목록을 조회하여 화면에 전달
    @GetMapping("/list")
    public String listContainers(Model model) {
        UserInfo user = authService.getCurrentUser(); // 현재 로그인 사용자
        String prefix = "ct-" + user.getUserId(); // 해당 사용자의 컨테이너 접두사

        var containers = containerManager.listUserContainers(prefix);
        model.addAttribute("containers", containers);
        return "container/list";
    }

    // 2. POST /container/stop/{containerName}
    // 실행 중인 컨테이너를 중지하는 기능
    @PostMapping("/stop/{containerName}")
    public String stopContainer(@PathVariable String containerName) {

        // 실행 중인 경우에만 stop
        if (containerManager.isRunning(containerName)) {
            containerManager.stopContainer(containerName);
        }

        return "redirect:/container/list";
    }

    // 3. POST /container/delete/{containerName}
    // 컨테이너 삭제 기능 (실행/중지 상태 관계 없음)
    @PostMapping("/delete/{containerName}")
    public String deleteContainer(@PathVariable String containerName) {

        // 실행 여부 상관 없이 강제 삭제
        containerManager.deleteContainer(containerName);

        return "redirect:/container/list";
    }

    // 4. POST /container/start/{containerName}
    // 중지된 컨테이너를 다시 실행 상태로 변경 (실행 중이면 아무 작업 없이 목록으로 돌아감)
    @PostMapping("/start/{containerName}")
    public String startContainer(@PathVariable String containerName) {
        if (!containerManager.isRunning(containerName)) {
            containerManager.startContainer(containerName);
        }
        return "redirect:/container/list";
    }

    // 5. POST /container/open/{containerName}
    // 실행 중인 컨테이너에 대해 접속 처리 (실행 중이 아닐 경우 접속 불가)
    @PostMapping("/open/{containerName}")
    public String openContainer(@PathVariable String containerName) {
        UserInfo user = authService.getCurrentUser();
        String prefix = "ct-" + user.getUserId();

        // 해당 사용자의 컨테이너인지 확인
        boolean exists = containerManager.containerExists(prefix, containerName);
        if (!exists) {
            return "redirect:/container/list"; // 또는 에러 페이지
        }

        // 실행 중이 아니라면 접속 불가
        if (!containerManager.isRunning(containerName)) {
            return "redirect:/container/list?error=not_running";
        }

        // 실행 중이면 바로 URL 조회 후 redirect
        String url = containerManager.getContainerUrl(containerName);
        return "redirect:" + url;
    }

    // 6. POST /container/create
    // 새로운 컨테이너를 생성하고 즉시 접속
    @PostMapping("/create")
    public String createContainer() {
        UserInfo user = authService.getCurrentUser();
        String url = containerManager.createContainer(user, serverDomain);
        return "redirect:" + url;
    }
}
