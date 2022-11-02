package hello.advanced.app.v2;

import hello.advanced.trace.TraceId;
import hello.advanced.trace.TraceStatus;
import hello.advanced.trace.hellotrace.HelloTraceV2;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
@Repository
@RequiredArgsConstructor
public class OrderRepositoryV2 {

    private final HelloTraceV2 trace;

    public void save(TraceId traceId, String itemId) {
        TraceStatus status = null;
        try {
            status = trace.beginSnyc(traceId,"orderRepository.save()");
            //저장 로직
            if(itemId.equals("ex")) {
                throw new IllegalStateException("예외 발생");
            }
            sleep(1000);
            trace.end(status);
        } catch (Exception e){
            trace.exception(status, e);
            throw e; //예외를 꼭 다시 던져주어야 한다.
        }
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
