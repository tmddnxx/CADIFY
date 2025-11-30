package com.cadify.cadifyWAS.service.file;

import com.cadify.cadifyWAS.config.ThreadLocalEnv;
import com.cadify.cadifyWAS.model.dto.files.FileTask;
import com.cadify.cadifyWAS.util.PrivateValue;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Log4j2
@Service
public class SdkService {

    private final PrivateValue privateValue;

    /* ------------------------------------------ 판금 START ------------------------------------------- */
    
    // 공장에서 실행
    public String executeDockerByFactory(String memberKey, String fileName, Double kFactor) {

        String stepFileName = fileName.replace(" ", "");
        String jsonFileName = fileName.replace(".stp", ".json").replace(" ", "");

        String basePath = privateValue.getServerRootDir();

        // 스레드로컬 스레드별 메모리영역에 환경변수 저장
        ThreadLocalEnv.setEnv("CONTAINER_NAME", "METAL_FACTORY");
        ThreadLocalEnv.setEnv("MEMBER_ID", memberKey);
        ThreadLocalEnv.setEnv("STEP_FILE", stepFileName);
        ThreadLocalEnv.setEnv("JSON_FILE", jsonFileName);
        ThreadLocalEnv.setEnv("K_FACTOR", String.valueOf(kFactor));
        log.info("스레드로컬 환경변수 : {}", ThreadLocalEnv.getAllEnv());

        // ProcessBuilder로 docker-compose 명령어 실행
        ProcessBuilder processBuilder = new ProcessBuilder(
                privateValue.getDockerPath(),
                "-p", "service_" + "factory",
                "-f", basePath + "/docker-compose.yaml",
                "up", "quaoar-sdk");

        Map<String, String> processEnv = processBuilder.environment();
        processEnv.putAll(ThreadLocalEnv.getAllEnv());
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);  // 로그 출력
                }
            }

            int exitCode = process.waitFor();

            log.info("Docker process exited with code: {}", exitCode);

            if(exitCode == 0){ // 도커 run이 성공적으로 실행되면 dxf 파일 경로 반환

                ProcessBuilder downProcessBuilder = new ProcessBuilder(
                        privateValue.getDockerPath(),
                        "-p", "service_" + "factory",
                        "-f", basePath + "/docker-compose.yaml",
                        "down"  // 종료 및 삭제
                );

                // 실행: docker-compose down
                Process downProcess = downProcessBuilder.start();
                int downExitCode = downProcess.waitFor();  // down 명령어가 완료될 때까지 대기
                if (downExitCode == 0) {
                    System.out.println("컨테이너가 성공적으로 삭제되었습니다.");
                }

                return findDxfFileForUpload(memberKey, fileName);

            }else{
                log.error("Docker process failed with exit code: {}", exitCode);
                return null;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    /* ------------------------------------------ 판금 END ------------------------------------------- */

    /* ------------------------------------------ 절삭 끝 ------------------------------------------- */

    @Scheduled(fixedRate = 86400000) // 24시간 마다 실행
    public void removeDockerContainer(){ // 24시간동안 실행안된 컨테이너 삭제
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("docker", "container", "prune", "--filter", "until=24h", "--force");
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if(exitCode == 0){
                log.info("컨테이너 삭제 완료");
            }else{
                log.info("컨테이너 삭제 실패, 코드 : {}", exitCode);
            }
        }  catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostConstruct
    public void initDockerNetwork() throws Exception {
        if (!isDockerNetworkExists("metal_network")) {
            createDockerNetwork("metal_network");
        }
        if (!isDockerNetworkExists("cnc_network")) {
            createDockerNetwork("cnc_network");
        }
    }

    // 네트워크가 존재하는지 확인하는 메서드
    public boolean isDockerNetworkExists(String networkName) throws Exception {
        Process checkProcess = new ProcessBuilder(privateValue.getOriginDockerPath(), "network", "ls", "--filter", "name=^" + networkName + "$", "--format", "{{.Name}}").start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(checkProcess.getInputStream()));
        boolean exists = reader.lines().anyMatch(line -> line.trim().equals(networkName));

        return exists;
    }

    // 네트워크를 생성하는 메서드
    public boolean createDockerNetwork(String networkName) throws Exception {
        if (isDockerNetworkExists(networkName)) {
            System.out.println("Network " + networkName + " already exists.");
            return true;  // 이미 네트워크가 존재하면 성공으로 간주
        }

        Process createProcess = new ProcessBuilder(privateValue.getOriginDockerPath(), "network", "create", networkName).start();
        int exitCode = createProcess.waitFor();
        if (exitCode == 0) {
            System.out.println("Network " + networkName + " created successfully.");
            return true;
        } else {
            System.err.println("Failed to create network " + networkName + ". Exit code: " + exitCode);
            return false;
        }
    }




    // 저장된 dxf 파일경로 찾기
    public String findDxfFileForUpload(String memberKey, String pattern) {
        String basePath = privateValue.getServerRootDir();
        Path jsonOutPath = Paths.get(basePath, "metal", "jsonOut", memberKey);
        pattern = pattern.split("\\.")[0];
        if (pattern.contains(" ")){
            pattern = pattern.replace(" ", "");
        }

        try (DirectoryStream<Path> stream = java.nio.file.Files.newDirectoryStream(jsonOutPath, "*.dxf")) {
            for (Path entry : stream) {
                // 파일 이름에 "파일명_타임스탬프"가 포함되어 있는지 확인
                if (entry.getFileName().toString().contains(pattern)) {
                    log.info("DXF 경로 : {}", entry.toString());
                    return entry.toString(); // 찾은 파일 경로 리턴
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null; // 파일을 찾지 못한 경우 null 반환
    }

    // 디렉토리 내 파일 삭제
    public void deleteFiles(Path path) throws IOException {

        Files.walk(path)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try{
                        Files.delete(file);
                    } catch (IOException e) {
                        log.info(e.getMessage());
                    }
                });

    }

}
