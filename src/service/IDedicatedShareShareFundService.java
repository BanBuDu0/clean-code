package service;

/**
 * @author syj
 */
public interface IDedicatedShareShareFundService extends INonDedicatedShareFundService {

    /**
     * calculate Dedicated
     *
     * @param city city index
     * @param t    time slot
     * @return Dedicated
     */
    double calculateDedicated(int city, int t);

}
