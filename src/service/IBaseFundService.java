package service;

/**
 * @author Sunyuejun
 */
public interface IBaseFundService {

    /**
     * calculate Shared
     *
     * @param t time slot
     * @return Shared
     */
    double[] calculateFund(int t);
}
