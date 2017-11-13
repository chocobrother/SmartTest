#include "kr_ac_sch_se_algorithm_bandpassfilter.h"
#include <jni.h>
#include <vector>
#include <exception>
#include <algorithm>
#include <Eigen/Dense>
#include <iterator>
#include <iostream>
#include <fstream>

#ifdef __cplusplus
    extern "C"{
#endif

        using namespace std;

        typedef std::vector<int> vectori;
        typedef std::vector<double> vectord;

        inline void add_index_range(vectori &indices, int beg, int end, int inc = 1)
        {
            for (int i = beg; i <= end; i += inc)
        indices.push_back(i);
}

inline void add_index_const(vectori &indices, int value, size_t numel)
{
    while (numel--)
        indices.push_back(value);
}

inline void append_vector(vectord &vec, const vectord &tail)
{
    vec.insert(vec.end(), tail.begin(), tail.end());
}

inline vectord subvector_reverse(const vectord &vec, int idx_end, int idx_start)
{
    vectord result(&vec[idx_start], &vec[idx_end + 1]);
    std::reverse(result.begin(), result.end());
    return result;
}

inline int max_val(const vectori& vec)
{
    return std::max_element(vec.begin(), vec.end())[0];
}

inline void filter(vectord B, vectord A, const vectord &X, vectord &Y, vectord &Zi)
{
    if (A.empty())
        throw std::domain_error("The feedback filter coefficients are empty.");
    if (std::all_of(A.begin(), A.end(), [](double coef) { return coef == 0; }))
        throw std::domain_error("At least one of the feedback filter coefficients has to be non-zero.");
    if (A[0] == 0)
        throw std::domain_error("First feedback coefficient has to be non-zero.");

    // Normalize feedback coefficients if a[0] != 1;
    auto a0 = A[0];
    if (a0 != 1.0)
    {
        std::transform(A.begin(), A.end(), A.begin(), [a0](double v) { return v / a0; });
        std::transform(B.begin(), B.end(), B.begin(), [a0](double v) { return v / a0; });
    }

    size_t input_size = X.size();
    size_t filter_order = std::max(A.size(), B.size());
    B.resize(filter_order, 0);
    A.resize(filter_order, 0);
    Zi.resize(filter_order, 0);
    Y.resize(input_size);

    const double *x = &X[0];
    const double *b = &B[0];
    const double *a = &A[0];
    double *z = &Zi[0];
    double *y = &Y[0];

    for (size_t i = 0; i < input_size; ++i)
    {
        size_t order = filter_order - 1;
        while (order)
        {
            if (i >= order)
                z[order - 1] = b[order] * x[i - order] - a[order] * y[i - order] + z[order];
            --order;
        }
        y[i] = b[0] * x[i] + z[0];
    }
    Zi.resize(filter_order - 1);
}

inline void filtfilt(vectord B, vectord A, const vectord &X, vectord &Y)
{
    using namespace Eigen;

    int len = X.size();     // length of input
    int na = A.size();
    int nb = B.size();
    int nfilt = (nb > na) ? nb : na;
    int nfact = 3 * (nfilt - 1); // length of edge transients

    if (len <= nfact)
        throw std::domain_error("Input data too short! Data must have length more than 3 times filter order.");

    // set up filter's initial conditions to remove DC offset problems at the
    // beginning and end of the sequence
    B.resize(nfilt, 0);
    A.resize(nfilt, 0);

    vectori rows, cols;
    //rows = [1:nfilt-1           2:nfilt-1             1:nfilt-2];
    add_index_range(rows, 0, nfilt - 2);
    if (nfilt > 2)
    {
        add_index_range(rows, 1, nfilt - 2);
        add_index_range(rows, 0, nfilt - 3);
    }
    //cols = [ones(1,nfilt-1)         2:nfilt-1          2:nfilt-1];
    add_index_const(cols, 0, nfilt - 1);
    if (nfilt > 2)
    {
        add_index_range(cols, 1, nfilt - 2);
        add_index_range(cols, 1, nfilt - 2);
    }
    // data = [1+a(2)         a(3:nfilt)        ones(1,nfilt-2)    -ones(1,nfilt-2)];

    auto klen = rows.size();
    vectord data;
    data.resize(klen);
    data[0] = 1 + A[1];  int j = 1;
    if (nfilt > 2)
    {
        for (int i = 2; i < nfilt; i++)
            data[j++] = A[i];
        for (int i = 0; i < nfilt - 2; i++)
            data[j++] = 1.0;
        for (int i = 0; i < nfilt - 2; i++)
            data[j++] = -1.0;
    }

    vectord leftpad = subvector_reverse(X, nfact, 1);
    double _2x0 = 2 * X[0];
    std::transform(leftpad.begin(), leftpad.end(), leftpad.begin(), [_2x0](double val) {return _2x0 - val; });

    vectord rightpad = subvector_reverse(X, len - 2, len - nfact - 1);
    double _2xl = 2 * X[len - 1];
    std::transform(rightpad.begin(), rightpad.end(), rightpad.begin(), [_2xl](double val) {return _2xl - val; });

    double y0;
    vectord signal1, signal2, zi;

    signal1.reserve(leftpad.size() + X.size() + rightpad.size());
    append_vector(signal1, leftpad);
    append_vector(signal1, X);
    append_vector(signal1, rightpad);

    // Calculate initial conditions
    MatrixXd sp = MatrixXd::Zero(max_val(rows) + 1, max_val(cols) + 1);
    for (size_t k = 0; k < klen; ++k)
        sp(rows[k], cols[k]) = data[k];
    auto bb = VectorXd::Map(B.data(), B.size());
    auto aa = VectorXd::Map(A.data(), A.size());
    MatrixXd zzi = (sp.inverse() * (bb.segment(1, nfilt - 1) - (bb(0) * aa.segment(1, nfilt - 1))));
    zi.resize(zzi.size());

    // Do the forward and backward filtering
    y0 = signal1[0];
    std::transform(zzi.data(), zzi.data() + zzi.size(), zi.begin(), [y0](double val) { return val*y0; });
    filter(B, A, signal1, signal2, zi);
    std::reverse(signal2.begin(), signal2.end());
    y0 = signal2[0];
    std::transform(zzi.data(), zzi.data() + zzi.size(), zi.begin(), [y0](double val) { return val*y0; });
    filter(B, A, signal2, signal1, zi);
    Y = subvector_reverse(signal1, signal1.size() - nfact - 1, nfact);
}

JNIEXPORT jdoubleArray JNICALL Java_kr_ac_sch_se_algorithm_bandpassfilter_calculation
        (JNIEnv *env, jclass thiz, jdoubleArray b, jdoubleArray a, jdoubleArray signal, jint signal_length) {

    cout<<"hello"<<endl;
    vector<double> input;
    vector<double> output;
    vector<double> b_coeff;
    vector<double> a_coeff;

    a_coeff.reserve(7);
    b_coeff.reserve(7);
    input.reserve(signal_length);

    double *a_ = env->GetDoubleArrayElements(a, 0);
    double *b_ = env->GetDoubleArrayElements(b, 0);
    double *signal_ = env->GetDoubleArrayElements(signal, 0);


    for(int n = 0; n < 7; n++){
        a_coeff.push_back(a_[n]);
    }

    for(int n = 0; n < 7; n++){
        b_coeff.push_back(b_[n]);
    }

    for(int n = 0; n < signal_length; n++){
        input.push_back(signal_[n]);
    }

    filtfilt(a_coeff, b_coeff, input, output);

    jdoubleArray resultArray = env->NewDoubleArray(output.size());
    jdouble *rdata = env->GetDoubleArrayElements(resultArray, 0);

    for(vector<int>::size_type i = 0; i < output.size(); i++){
        rdata[i] = output[i];
    }

    env->SetDoubleArrayRegion(resultArray, 0, output.size(), rdata);

    return resultArray;
}

JNIEXPORT void JNICALL Java_kr_ac_sch_se_algorithm_bandpassfilter_printTest
        (JNIEnv *env, jclass thiz){

}

#ifdef __cplusplus
}
#endif
