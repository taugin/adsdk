重要问题总结
1. 输出所有混淆配置文件
   如果需要输出所有混淆配置文件，可以在混淆文件里面增加如下语句：
   -printconfiguration full-r8-config.txt
2. AAB转ABK失败原因
   com.google.android.material:material版本过高，降低为1.6.0及以下版本
3. 如何更新gradle8之后，混淆实在有问题的话，就在gradle.properties 文件里修改
   一句什么fullmodel = false，关闭R8编译优化的fullmodel，在gradle8 之前R8编译优化为了兼容以前的模式就没有全开