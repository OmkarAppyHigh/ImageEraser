# ImageEraser

## What
An Image Eraser library to remove background from images.

## Initialization


In your  `build.gradle(project)`:

```groovy

allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

```

In your  `build.gradle(app)`:

```groovy

dependencies {
   implementation 'com.github.OmkarAppyHigh:ImageEraser:1.0.3'
}

```

## Usage

Start Eraser Screen with below code

```kotlin

  Eraser.activity()
        .src(uri)
        .start(this)

```

<p float="left">
  <img src="/screenshots/before.jpg" width="200" />
  <img src="/screenshots/after.jpg" width="200" /> 
</p>

### Getting the result

```kotlin

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Eraser.ERASER_ACTIVITY_REQUEST_CODE){
            when(resultCode){
                Activity.RESULT_OK -> {
                    val uri = Eraser.getUri(data)
                }
                Eraser.ERASER_ACTIVITY_RESULT_ERROR_CODE -> {
                    val ex = Eraser.getError(data)
                }
            }
        }
    }

```

## Options

### You can get result image path instead of URI using following code

```kotlin
    Eraser.activity()
            .src(uri)
            .shouldReturnResultPath(true)
            .start(this)     
```


```kotlin
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Eraser.ERASER_ACTIVITY_REQUEST_CODE){
            when(resultCode){
                Activity.RESULT_OK -> {
                    val path = Eraser.getResultPath(data)
                }
                Eraser.ERASER_ACTIVITY_RESULT_ERROR_CODE -> {
                    val ex = Eraser.getError(data)
                }
            }
        }
    }
```

### Set output image ratio 
You can set output image ration using method - setImageRatio(ratio: String)
```kotlin
    Eraser.activity()
        .src(uri)
        .setImageRatio("1:1")
        .start(this)  
```


## Customization

#### Set background color using - setBackgroundColor(@ColorRes color: Int)
```kotlin
    Eraser.activity()
        .src(uri)
        .setBackgroundColor(R.color.colorPrimary)
        .start(this)  
```

#### Set tools background color using - setToolBarBackgroundColor(@ColorRes color: Int)
```kotlin
    Eraser.activity()
        .src(uri)
        .setToolBarBackgroundColor(R.color.colorSecondary)
        .start(this)  
```

#### Set Done button color using - setButtonColor(@ColorRes color: Int)
```kotlin
    Eraser.activity()
        .src(uri)
        .setButtonColor(R.color.colorPrimary)
        .start(this)  
```


#### Set Eraser seekbar color using - setSeekbarColor(@ColorRes color: Int)
```kotlin
    Eraser.activity()
        .src(uri)
        .setSeekbarColor(R.color.colorPrimary)
        .start(this)  
```




