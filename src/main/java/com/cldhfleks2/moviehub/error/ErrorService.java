package com.cldhfleks2.moviehub.error;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
//필드 주입 필요없이 static send 메소드를 사용해서 오류내용을 출력
public class ErrorService {
    //각 요청별로 독립적인 에러 속성을 저장 (Model model을 사용하지 않기 위함)
    //각 요청이 끝나면 알아서 JVM이 errorContext를 정리함. errorContext.remove를 할 필요가 없음
    private static final ThreadLocal<Map<String, Object>> errorContext = new ThreadLocal<>();

    /**
     * return ErrorService.send()로 사용한다.
     * ResponsEntity에서는 사용 불가
     * @param status     HTTP 상태 코드 (HttpStatus.UNAUTHORIZED, HttpStatus.NOT_FOUND등)
     * @param path       에러가 발생한 요청 URL
     * @param message    오류 메시지
     * @param returnType String.class or ResponseEntity.class
     * @return           String.class: error페이지, ResponseEntity.class: JSON에러메시지
     */
    public static <T>T send(int status, String path, String message, Class<T> returnType) {
        //에러 내용을 콘솔에도 추가
        System.out.println("에러발생 >>>   " + path + "   ,  메시지 >>>   " + message + "   returnType >>> " + returnType);
        if(returnType.equals(String.class)){
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("status", status);
            errorInfo.put("path", path);
            errorInfo.put("message", message);
            errorContext.set(errorInfo);
            return returnType.cast("error");

        }else if(returnType.equals(ResponseEntity.class)){
            // ResponseEntity 반환하는 로직
            return returnType.cast(ResponseEntity.status(status).body(
                    String.format("{\"status\":%d,\"path\":\"%s\",\"message\":\"%s\"}",
                            status,
                            path,
                            message)
            ));
        }

        throw new IllegalArgumentException("Unsupported return type");
    }


    // 에러 속성을 보기 (아직 사용 안함)
    public static Map<String, Object> getErrorContext() {
        return errorContext.get();
    }

}
