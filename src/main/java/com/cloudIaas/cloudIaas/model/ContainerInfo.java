package com.cloudIaas.cloudIaas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContainerInfo {
    private String name;      // 컨테이너 이름
    private String statusText;    // docker ps 에서 가져온 원본 상태 문자열
    private boolean running;  // 실행 여부-프론트(UI)를 위한 정보 (Up → true / Exited → false)
    private String url;       // 접속 가능한 URL (목록에서는 null일 수 있음)
}
