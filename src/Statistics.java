/**
 * Ricart and Agrawala's Algoritm simulaton
 * ----------------------------------------
 * Student : 撑タ
 * Date    : 2017/12/18
 * Major   : Distributed System
 * Filename: Statistics.java
 */
class Statistics {
    // max  statistic ┮Τ计程
    private double max;
    // min  statistic ┮Τ计程
    private double min;
    // total  statistic ┮Τ计羆㎝
    private double total;
    // sqr_total  statistic ┮Τ计キよ㎝
    private double sqr_total;
    // count  statistic ┮Τ计计
    private int count;

    public Statistics() {
        max = min = total = sqr_total = 0.0;
        count = 0;
    }

    /**
     * 穝糤参璸戈
     *
     * @param value 璶参璸璸衡ぃ箂
     */
    public void add(double value) {
        // パ璶参璸戈ぃ穦 0┮肚ゑ 0 计琌岿粇
        assert (value > 0) : "statistic.add(value) is less than 0";

        // min 砞Θ单材计
        if (++count == 1)
            min = value;

        // 穝程籔程
        if (value > max)
            max = value;
        else if (value < min)
            min = value;

        // 盢 value キよ sqr_total
        sqr_total += value * value;

        // 盢 value  total
        total += value;
    }

    /**
     * 肚参璸戈キА
     *
     * @return 肚参璸戈キА
     */
    public double getMean() {
        return (total / count);
    }

    /**
     * 肚参璸戈程
     *
     * @return 肚参璸戈程
     */
    public double getMaximum() {
        return max;
    }

    /**
     * 肚参璸戈程
     *
     * @return 肚参璸戈程
     */
    public double getMinimum() {
        return min;
    }

    /**
     * 肚参璸戈跑钵计
     *
     * @return 肚参璸戈跑钵计
     */
    double getVariance() {
        // variance そΑ
        return sqr_total / count - getMean() * getMean();
    }
}
