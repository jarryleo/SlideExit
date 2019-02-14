# SlideExit
## 侧滑关闭activity控件

本库不用设置主题，一句代码使用：         
在application里面初始化即可：
```
SwipeBack.init(this);
```

#### 依赖方法：
1.在项目的全局build文件里面添加仓库：
```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
2.在app的build文件里面添加依赖：
```
dependencies {
      ...
	    implementation 'com.github.jarryleo:SlideExit:v1.3'
}
```

#### 注意事项：
> 1.本库自带页面过渡动画，不用再重写activity过渡动画，可能会导致错乱            
> 2.如果某个页面滑动冲突不好解决，可以在activity类上打上注解：@SwipeBack.IgnoreSwipeBack关闭当前页面的滑动关闭


