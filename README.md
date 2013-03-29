饭否Android客户端开源版
===========================
    
---
Introduction
===========================

  饭否Android客户端是饭否官方开发和维护的Android客户端2011年9月1日发布1.0版，2012年1月16日发布1.8.8版，此后由于各种原因没有进行功能更新，2013年3月我抽出时间增加了几项功能，于2013年3月21日发布了1.9.1版，之后可能没有时间继续开发
  
  征得饭否创始人王兴同意后，将项目代码采用Apache License, Version 2.0开源
  
  感谢王兴创建了饭否，感谢所有曾经的饭否员工，感谢所有饭否用户，感谢所有支持和热爱饭否的人

---

Project
===========================
    饭否Android客户端采用标准Android项目结构：

        main/app目录为客户端项目代码
        libs/pulltorefresh目录为依赖的下拉刷新项目
        项目的包名已修改为com.fanfou.app.opensource，可以和原版共存
        项目不包含饭否的OAuth API KEY，请自行添加
        项目不包含饭否的LOGO和1.9.1版的图片滤镜功能
        项目不包含签名用的KeyStore，请自行发布添加
        项目采用UTF-8编码，请修改Eclipse的项目设置

---

Usage
===========================

####API KEY
    在进行修改和编译之前，请找到main/app/res/values/api.xml文件，在里面填入你申请的饭否OAuth API KEY
    如果没有，可以去(<http://fanfou.com/apps>)申请

####Using Eclipse
    直接使用File->Import导入pulltorefresh和app两个项目，Clean&Build即可
    如果需要导出release版，请用自己创建的KeyStore签名
    
####Using ANT
    配置好ANT后，终端进入main/app目录，输入命令ant debug
    如果要构建release版，需要在main/app/ant.properties里面填写keystore相关的配置，然后输入命令ant release
    
####Using Maven
    只支持Maven 3.0以上的版本
    配置好Maven后，终端进入main/app目录，输入命令mvn clean install
    
---
    
Others
===========================
    1. 此项目开源后不会增加新功能，有功能需求请自行fork修改
    2. 有问题可以在https://github.com/mcxiaoke/fanfouapp/issues提
    3. 欢迎Pull Request，代码采用Eclipse四空格缩进格式
    
---

Developed By
===========================
    Xiaoke Zhang (@mcxiaoke)
    
---

Websites
===========================
    饭否官网 http://fanfou.com
    饭否创始人王兴 http://fanfou.com/wangxing
    Android客户端 http://apps.fanfou.com/android/
    API KEY申请 http://fanfou.com/apps
    饭否API文档 https://github.com/FanfouAPI/FanFouAPIDoc/wiki

License
===========================

    Copyright 2013 fanfou.com, mcxiaoke

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.





