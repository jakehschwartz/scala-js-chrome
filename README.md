# Chrome for Scala.js [![Build Status](https://travis-ci.org/AlexITC/scala-js-chrome.svg?branch=master)](https://travis-ci.org/AlexITC/scala-js-chrome) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/scala-js-chrome/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [![Latest version](https://index.scala-lang.org/AlexITC/scala-js-chrome/scala-js-chrome/latest.svg?color=orange)](https://index.scala-lang.org/AlexITC/scala-js-chrome/scala-js-chrome)

The goal of this project is to provide an easy and typesafe way to create Chrome
apps and extensions in Scala using the [Scala.js](https://www.scala-js.org/) project.

**DISCLAIMERS**:
- As of today (2020-03-30), I decided to fork the plugin so that I can keep applying updates and releasing new versions, the main reason to do this is to migrate to scalajs 1.0.0, which support is still experimental, if you use an older scalajs version, use the original [plugin](https://github.com/lucidd/scala-js-chrome) instead.
- Versions after `v0.6.1` replaced the manifest encoding, while it has been tested on some browser extensions, there might be errors, if you find any while installing the extension/app, try the `v0.6.1` version to see if the error disappears, in any case, please submit an issue with your manifest details, or even better, submit a pull request.


## Chrome API bindings

The bindings provide access to the Chrome app and extension APIs. There are two
levels for each API. One that provides the raw JavaScript bindings and a second
one which wraps the raw API in a more Scala idiomatic way.

The package structure is similar to the original JavaScript API.

```javascript
// original JavaScript
chrome.system.cpu.getInfo(function(info){
  if (chrome.runtime.lastError === undefined) {
    console.log(info);
  } else {
    console.log("ohoh something went wrong!");
  }
});
```

```scala
// raw bindings
chrome.system.cpu.bindings.CPU.getInfo((info: CPUInfo) => {
    if (chrome.runtime.bindings.Runtime.lastError.isEmpty) {
        println(info)
    } else {
        println("ohoh something went wrong!")
    }
})

// Scala idiomatic way using Future
chrome.system.cpu.CPU.getInfo.onComplete {
  case Success(info) => println(info)
  case Failure(error) => println("ohoh something went wrong!")
}
```

The Scala idiomatic binding provides the following general changes:

- Futures instead of callbacks
- Error handling using types like `Future` / `Try` instead of global error 
variable.
- Using `Option` for things that may or may not be defined.

## SBT Plugin

The job of the SBT plugin is to help with common tasks for developing Chrome
apps/extensions. It also provides a way to configure your app/extension in your
SBT file and automatically generate the manifest file.

- `chromePackage` will create a ZIP file you can upload to the Chrome Web Store.
- `chromeUnpackedOpt` (or `chromeUnpackedFast`) will build your projects with (or without) optimizations enabled. The
output will be in `target/chrome/unpacked-opt` (or `target/chrome/unpacked-fast`) and can be loaded by Chrome as an
unpacked extension/app.

## Getting Started

If you like to get an extension working fast, just follow the [chrome-scalajs-template](https://github.com/AlexITC/chrome-scalajs-template) instead.

Add this to your `project/plugins.sbt`:

```scala
addSbtPlugin("com.alexitc" % "sbt-chrome-plugin" % "0.7.3")
```

Add this to your project dependencies:

```scala
"com.alexitc" %%% "scala-js-chrome" % "0.7.3"
```

If you need the latest version, clone the repository and run `sbt version` on it, replace the current version with that value.
You may need to add this line to resolve the snapshot version: `resolvers += Resolver.sonatypeRepo("snapshots")`


### with [scalajs-bundler](https://scalacenter.github.io/scalajs-bundler/)
to use the `<project-name>-f{ast,ull}opt-bundle.js` generated by [scalajs-bundler](https://scalacenter.github.io/scalajs-bundler) add the following to your `build.sbt`:

```scala
fastOptJsLib := Attributed.blank((webpack in (Compile, fastOptJS)).value.head)
fullOptJsLib := Attributed.blank((webpack in (Compile, fullOptJS)).value.head)
```

_NOTE:_ if code seems to be executing duplicate times unintentionally, try removing these lines from the project's `build.sbt`

```scala
scalaJSUseMainModuleInitializer := true
scalaJSUseMainModuleInitializer in Test := false
```

### Creating a basic Window

```scala
import chrome.app.runtime.bindings.LaunchData
import chrome.app.window.Window
import utils.ChromeApp

import scalajs.concurrent.JSExecutionContext.Implicits.queue

object ChromeAppExample extends ChromeApp {

  override def onLaunched(launchData: LaunchData): Unit = {
    println("hello world from scala!")
    Window.create("assets/html/App.html").foreach { window =>
      /**
         Access to the document of the newly created window.
         From here you can change the HTML of the window with whatever
         library you want to use.
      */
      window.contentWindow.document
    }
  }

}
```

For a more complete example see [chrome-system-monitor](https://github.com/lucidd/chrome-system-monitor)
and [scala-js-chrome examples](/examples).

### UI Libraries

There are already multiple libraries to manipulate HTML and build your UI
available for Scala.js:

- [scala-js-dom](https://github.com/scala-js/scala-js-dom) For simple dom access
- [scalatags](https://github.com/lihaoyi/scalatags) Scala DSL for creating HTML
- [scalajs-react](https://github.com/japgolly/scalajs-react) Bindings for using react.js
- [scalacss](https://github.com/japgolly/scalacss) Typesafe way to write CSS
  with Scala
- [Binding.scala](https://github.com/ThoughtWorksInc/Binding.scala) Reactive data-binding for Scala

### Known Issues

In Chrome apps and extensions there are multiple places where you can run
JavaScript. Normally you split your logic into different files and load them into
whatever context they need to run. Since Scala.js compiles your whole project
into one big file all contexts need to load this big file with all the logic
even if they only need a small subset. This can cause your app you use more
memory then it need to. In some cases this can be worked around for example the
a background page can manipulate the DOM of an App window so you don't need any
JavaScript at all in the window itself.
