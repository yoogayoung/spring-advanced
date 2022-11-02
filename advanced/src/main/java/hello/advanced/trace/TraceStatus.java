package hello.advanced.trace;

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
public class TraceStatus {

    private TraceId traceId;
    private Long startTimeMs; //로그 시작시간. 로그 종료 시 전체 수행 시간을 구함.
    private String message; //시작 시 사용한 메시지. 종료 시에도 이 메시지를 출력한다.

    public TraceStatus(TraceId traceId, Long startTimeMs, String message) {
        this.traceId = traceId;
        this.startTimeMs = startTimeMs;
        this.message = message;
    }

    public TraceId getTraceId() {
        return traceId;
    }

    public Long getStartTimeMs() {
        return startTimeMs;
    }

    public String getMessage() {
        return message;
    }
}
