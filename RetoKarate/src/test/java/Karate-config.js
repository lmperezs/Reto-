function fn() {
    karate.configure('connectTimeout', 10000);
    karate.configure('readTimeout', 10000);
    karate.configure ('ssl', true);
    var env = karate.env; // get java system property 'karate.env' in  build.gradle
    var account = env == 'dev' ? '360735510274' : '278078741213';
    karate.log('karate.env system property was:', env);

    return {
            aws: {
              lambdaFunctionName: 'PruebaLambda',
              region: 'us-east-1'
            }
    };
}