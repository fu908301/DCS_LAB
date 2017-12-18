/**
 * Ricart and Agrawala's Algoritm simulaton
 * ----------------------------------------
 * Student : �ť��w
 * Date    : 2017/12/18
 * Major   : Distributed System
 * Filename: Statistics.java
 */
class Statistics {
    // max  statistic �Ҧ��ƪ��̤j��
    private double max;
    // min  statistic �Ҧ��ƪ��̤p��
    private double min;
    // total  statistic �Ҧ��ƪ��`�M
    private double total;
    // sqr_total  statistic �Ҧ��ƪ�����M
    private double sqr_total;
    // count  statistic �Ҧ��ƪ��Ӽ�
    private int count;

    public Statistics() {
        max = min = total = sqr_total = 0.0;
        count = 0;
    }

    /**
     * �s�W�@�Ӳέp���
     *
     * @param value �@�ӭn�[�J�έp�p��B���p��s����
     */
    public void add(double value) {
        // �ѩ�n�έp����Ƥ��|�p�� 0�A�ҥH�ǤJ�� 0 �p���ƬO���~��
        assert (value > 0) : "statistic.add(value) is less than 0";

        // min ���]������Ĥ@�ӥ[�J����
        if (++count == 1)
            min = value;

        // ��s�̤j�ȻP�̤p��
        if (value > max)
            max = value;
        else if (value < min)
            min = value;

        // �N value �����[�J sqr_total
        sqr_total += value * value;

        // �N value ���ȥ[�J total
        total += value;
    }

    /**
     * �^�ǲέp��ƪ�������
     *
     * @return �^�ǲέp��ƪ�������
     */
    public double getMean() {
        return (total / count);
    }

    /**
     * �^�ǲέp��ƪ��̤j��
     *
     * @return �^�ǲέp��ƪ��̤j��
     */
    public double getMaximum() {
        return max;
    }

    /**
     * �^�ǲέp��ƪ��̤p��
     *
     * @return �^�ǲέp��ƪ��̤p��
     */
    public double getMinimum() {
        return min;
    }

    /**
     * �^�ǲέp��ƪ��ܲ���
     *
     * @return �^�ǲέp��ƪ��ܲ���
     */
    double getVariance() {
        // variance ������
        return sqr_total / count - getMean() * getMean();
    }
}
