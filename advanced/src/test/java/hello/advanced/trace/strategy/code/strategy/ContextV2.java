package hello.advanced.trace.strategy.code.strategy;

import lombok.extern.slf4j.Slf4j;

/**
 * Class 설명
 *
 * @author yoo.gayoung
 * @version 1.0
 * @see == 개정이력(Modification Information) ==
 * <p>
 * 수정일      수정자           수정내용
 * -------    --------    ---------------------------
 * @since 2022-09-29
 */
@Slf4j
public class ContextV2 {

    //스프링에서는 이와 같은 방식의 전략 패턴을 템플릿 콜백 패턴이라 한다.
    public void execute(Strategy strategy) { // 전략을 필드로 가지지 않고, excute가 호출될 때마다 항상 파라미터로 전달 받는다.
        long startTime = System.currentTimeMillis();
        //비즈니스 로직 실행
        strategy.call(); //위임
        //비즈니스 로직 종료
        long endTime = System.currentTimeMillis();
        long resultTime = endTime - startTime;
        log.info("resultTime={}", resultTime);
    }

}