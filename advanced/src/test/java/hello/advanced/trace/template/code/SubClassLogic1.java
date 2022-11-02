package hello.advanced.trace.template.code;

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
 * @since 2022-09-27
 */
@Slf4j
public class SubClassLogic1 extends AbstractTemplate {

    @Override
    protected void call(){
        log.info("비즈니스 로직1 실행");
    }
}
