package hello.advanced.trace.hellotrace;

import hello.advanced.trace.TraceId;
import hello.advanced.trace.TraceStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Class 설명
 *
 * @author yoo.gayoung
 * @version 1.0
 * @see == 개정이력(Modification Information) ==
 * <p>
 * 수정일      수정자           수정내용
 * -------    --------    ---------------------------
 * @since 2022-09-21
 */
@Slf4j
@Component
public class HelloTraceV2 { //싱글톤

    private static final String START_PREFIX = "-->";
    private static final String COMPLATE_PREFIX = "<--";
    private static final String EX_PREFIX = "<X-";

    public TraceStatus begin(String message){
        TraceId traceId = new TraceId();
        Long startTimeMs = System.currentTimeMillis();
        //시작로그 출력
        log.info("[{}] {}{}", traceId.getId(), addSpace(START_PREFIX, traceId.getLevel()), message);
        return new TraceStatus(traceId, startTimeMs, message);
    }

    //v2에서 추가
    public TraceStatus beginSnyc(TraceId beforeTraceId, String message){
        TraceId nextId = beforeTraceId.createNextId();
        Long startTimeMs = System.currentTimeMillis();
        //시작로그 출력
        log.info("[{}] {}{}", nextId.getId(), addSpace(START_PREFIX, nextId.getLevel()), message);
        return new TraceStatus(nextId, startTimeMs, message);
    }

    public void end(TraceStatus status){
        complate(status, null);
    }
    public void exception(TraceStatus status, Exception e){
        complate(status, e);
    }

    private void complate(TraceStatus status, Exception e){
        Long stopTimeMs = System.currentTimeMillis();
        long resultTimeMs = stopTimeMs - status.getStartTimeMs();
        TraceId traceId = status.getTraceId();
        //종료 로그 또는 예외 로그 출력
        if(e == null){ //예외가 없으면
            log.info("[{}] {}{} time={}ms", traceId.getId(), addSpace(COMPLATE_PREFIX, traceId.getLevel()), status.getMessage(), resultTimeMs);
        }else{
            log.info("[{}] {}{} time={}ms ex={}", traceId.getId(), addSpace(COMPLATE_PREFIX, traceId.getLevel()), status.getMessage(), resultTimeMs, e.toString());
        }
    }

    /*
    * prifix: -->
    *   level 0:''
    *   level 1:'|-->'
    *   level 2:'|   |-->
    * prefix: <--
    *   level 0:''
    *   level 1:|<--
    *   level 2:|   |<--
    * prefix: <X-
    *   level 0:''
    *   level 1:'|<X-'
    *   level 2:'|   |<X-'
    * */
    private static String addSpace(String prefix, int level){
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<level; i++){
            sb.append((i==level-1) ? "|" + prefix : "|   ");
        }
        return sb.toString();
    }
}
