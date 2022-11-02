package hello.advanced.trace;

import java.util.UUID;

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
public class TraceId {

    private String id; //트랜잭션 id
    private int level; //트랜잭션 깊이

    public TraceId() {
        this.id = createId();
        this.level = 0;
    }

    private TraceId(String id, int level){
        this.id = id;
        this.level = level;
    }

    private String createId() {
        return UUID.randomUUID().toString().substring(0,8);
    }

    public TraceId createNextId() { //id는 똑같고 레벨은 하나씩 증가
        return new TraceId(id, level+1);
    }

    public TraceId createPreviousId() { //id는 똑같고 레벨은 하나씩 감사
        return new TraceId(id, level-1);
    }

    public boolean isFirstLevel() { //첫번째 레벨 여부를 편리하게 확인할 수 있는 메서드
        return level == 0;
    }

    public String getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

}
