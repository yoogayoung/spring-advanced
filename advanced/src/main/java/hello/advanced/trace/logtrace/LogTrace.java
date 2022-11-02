package hello.advanced.trace.logtrace;

import hello.advanced.trace.TraceStatus;

/**
 * Class 설명
 *
 * @author yoo.gayoung
 * @version 1.0
 * @see == 개정이력(Modification Information) ==
 * <p>
 * 수정일      수정자           수정내용
 * -------    --------    ---------------------------
 * @since 2022-09-22
 */
public interface LogTrace {

    TraceStatus begin(String message);

    void end(TraceStatus status);

    void exception(TraceStatus status, Exception e);
}
