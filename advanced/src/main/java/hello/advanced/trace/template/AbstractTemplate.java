package hello.advanced.trace.template;

import hello.advanced.trace.TraceStatus;
import hello.advanced.trace.logtrace.LogTrace;

/**
 * Class 설명
 *
 * @author yoo.gayoung
 * @version 1.0
 * @see == 개정이력(Modification Information) ==
 * <p>
 * 수정일      수정자           수정내용
 * -------    --------    ---------------------------
 * @since 2022-09-27
 */
public abstract class AbstractTemplate<T> {

    private final LogTrace trace;

    public AbstractTemplate(LogTrace trace){ //생성자를 통한 주입
        this.trace = trace;
    }

    public T execute(String message){
        TraceStatus status = null;
        try {
            status = trace.begin(message);

            //로직 호출
            T result = call();

            trace.end(status);
            return result;
        } catch ( Exception e ){
            trace.exception(status, e);
            throw e;
        }
    }

    //이 부분은 상속으로 구현해야 한다.
    protected abstract T call();

}
