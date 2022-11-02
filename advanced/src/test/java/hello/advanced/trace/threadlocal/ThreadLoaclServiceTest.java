package hello.advanced.trace.threadlocal;

import hello.advanced.trace.threadlocal.code.ThreadLocalService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

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
@Slf4j
public class ThreadLoaclServiceTest {

    private ThreadLocalService service = new ThreadLocalService();

    @Test
    void field() {
        log.info("main start");

        /* 람다식 푼거
        Runnable userA = new Runnable() {
            @Override
            public void run() {
                fieldService.logic("userA");
            }
        };
         */

        Runnable userA = () -> {
            service.logic("userA");
        };
        Runnable userB = () -> {
            service.logic("userB");
        };

        Thread threadA = new Thread(userA);
        threadA.setName("thread-A");
        Thread threadB = new Thread(userB);
        threadB.setName("thread-B");

        threadA.start(); //A실행

        //sleep(2000); //동시성 문제 발생X -> 로직에서 1초 쉬는데 사이에 2초를 쉬니까
        sleep(100); //동시성 문제 발생O -> 첫번째 끝나기도 전에 0.1초만에 두번째 실행

        threadB.start(); //B실행

        sleep(3000); //메인 쓰레드 종료 대기

        log.info("main exit");
    }
    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
