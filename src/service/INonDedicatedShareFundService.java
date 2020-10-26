package service;

/**
 * @author syj
 */
public interface INonDedicatedShareFundService extends IBaseFundService {

    /**
     * calculate Over Factor
     *
     * @param t    time slot
     */
    void calculateOverFactor(int t);

    /**
     * calculate Ideal Shard
     *
     * @param city city index
     * @param t    time slot
     * @return IdealShard
     */
    double calculateIdealShard(int city, int t);
}
