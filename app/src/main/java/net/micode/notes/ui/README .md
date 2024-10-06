# ui文件夹后半部分源码分析

先用**SonarLint**整体扫一遍，发现ui模块中的十四个文件中有180个问题。
![uiproblems](https://i.imgur.com/wsym8zI.png)

但这其中有很多问题是因时代原因和重复的。所以需要人工复审一下。复审就很容易知道，其中一大半都是命名规范，或是因时代导致当时的函数如今被废弃了。

在SonarQube中，代码缺陷分为以下五个级别：
- **Blocker**: 极有可能影响应用程序表现的错误;
- **Critical**: 可能影响应用程序表现的错误和表示安全缺陷的问题;
- **Major**:严重影响开发者效率的质量缺陷:.
- **Minor**:轻微影响开发者效率的质量缺陷;.
- **Info**:不是错误或者质量缺陷。
所以在下面代码质量检测模块中将主要分析缺陷等级为Blocker、Critical、Major的部分：

## NoteEditActivity.java类源码分析
###  **类结构**:

- **类名**: `NoteEditActivity`
- **包名**: `net.micode.notes.ui`
- **依赖库**: 
  - Android SDK classes: 
    - `Activity`: 用于创建应用的活动界面。
    - `AlertDialog`: 用于创建和显示对话框。
    - `Context`: 提供对应用环境的访问。
    - `DialogInterface`: 提供对话框的接口，包含处理点击事件的方法。
    - `Intent`: 用于启动新活动或服务的范围。
    - `AudioManager`:管理音频模式和音量。
    - `MediaPlayer`:用于播放音频和视频。
    - `RingtoneManager`:管理系统铃声和通知声。
    - `Uri`:用于表示资源的统一资源标识符。
    - `Bundle`:用于传递数据的键值对集合。
    - `PowerManager`:控制设备电源状态。
    - `Settings`:提供访问系统设置的功能。
    - `Window`:用于操作窗口的外观和行为。
    - `WindowManager`:用于管理应用的窗口。
  - Java SDK classes:
    - `IOException`: 处理输入输出异常。
  - 其他依赖库:
    - `net.micode.notes.R`:应用资源的引用类。
    - `net.micode.notes.data.Notes`:用于管理便签数据的类。
    - `net.micode.notes.tool.DataUtils`:提供数据处理工具的方法。

###  **使用SonarLint进行代码质量检测**:

#### 问题：**"static" base class members should not be accessed via derived types.(“静态”基类成员不应通过派生类型访问。)**

![problem1](https://i.imgur.com/4bjvCrR.png)

静态成员（方法或属性）是在类层级上定义的，而不是在具体的实例层级上。静态成员是共享的，无论该类有多少个实例，所有实例共享一个静态成员。
派生类继承了基类的行为和状态，但静态成员不会跟随类的继承链走。因此，当通过派生类访问基类的静态成员时，可能会造成逻辑混淆。它暗示着静态成员与派生类关联，但实际上静态成员与派生类无关。
静态成员属于基类，如果通过派生类来访问这些静态成员，开发者可能会误以为这些成员是派生类独有的，或者与派生类直接关联。这可能导致误解或难以维护的代码。
SonarLint中给出的例子如下：
``` java
class Parent {
  public static int counter;
}

class Child extends Parent {
  public Child() {
    Child.counter++;  // 不推荐
  }
}
```

``` java
class Parent {
  public static int counter;
}

class Child extends Parent {
  public Child() {
    Parent.counter++;   //应当改为这种形式，让父对象去访问static属性
  }
}
```

这里是因为 `SPAN_INCLUSIVE_EXCLUSIVE` 是 `Spanned` 接口的一个静态常量，推荐直接通过类名来访问，而不是通过实例。
首先，`SPAN_INCLUSIVE_EXCLUSIVE` 是一个静态常量，使用 `Spanned.SPAN_INCLUSIVE_EXCLUSIVE` 可以提高代码的可读性，明确指明常量的来源。
其次，使用类名来访问静态成员能够使代码更具自解释性，让读者清楚常量的上下文。
再者，在派生类中通过 `this` 访问静态成员可能引起对成员来源的混淆，特别是在多层继承的情况下。
因此，这里可以将代码中的 `Spannable.SPAN_INCLUSIVE_EXCLUSIVE` 替换为 `Spanned.SPAN_INCLUSIVE_EXCLUSIVE`，以增强代码的清晰度和可维护性。

``` jave
spannable.setSpan(
    new BackgroundColorSpan(this.getResources().getColor(R.color.user_query_highlight)),
    m.start(), m.end(),
    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
);
```

#### 问题：**Cognitive Complexity of methods should not be too high。(方法的认知复杂度不应过高)**

![problem2](https://i.imgur.com/MxghhzD.png)

认知复杂度是衡量理解代码单元控制流难易程度的一种指标。具有高认知复杂度的代码难以阅读、理解、测试和修改。作为一个经验法则，高认知复杂度表明代码应该被重构为更小、更易于管理的部分。

以下是影响认知复杂度评分的代码语法：

1. **控制流破坏**：每当代码打破正常的线性阅读流时，认知复杂度就会增加。这包括循环结构、条件语句、异常捕获、开关语句、标签跳转以及混合多个操作符的条件。

2. **嵌套层次**：每增加一个嵌套层次，复杂度就会增加。在阅读代码时，嵌套层次越深，保持上下文的难度就越大。

3. **方法调用**：方法调用是免费的。一个恰当命名的方法可以总结多行代码。读者可以先从高层次了解代码的功能，再通过查看被调用函数的内容深入理解。但注意：这不适用于递归调用，递归调用会增加认知复杂度评分。

**例外情况**：`equals` 和 `hashCode` 方法被忽略，因为它们可能是自动生成的，并且在存在许多字段时可能难以理解。

在这段代码存在问题的原因是 `onOptionsItemSelected` 方法中包含多个 `case` 语句和嵌套结构。这些控制流语句使得代码的阅读变得更加复杂。每增加一个控制流结构，认知复杂度就会增加。并且许多 `case` 中包含了多个嵌套的 `AlertDialog` 和匿名内部类。这些嵌套使得代码上下文变得难以跟踪，增加了认知负担。而且在每个 `case` 中，执行多个操作，这使得每个 `case` 的复杂性进一步增加。

这里可以将每个 `case` 的处理逻辑拆分成独立的方法，使得每个方法更简洁、更易于理解。

``` java
public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
        case R.id.menu_new_note:
            createNewNote();
            break;
        case R.id.menu_delete:
            showDeleteConfirmationDialog();
            break;
        case R.id.menu_font_size:
            showFontSizeSelector();
            break;
        case R.id.menu_list_mode:
            toggleCheckListMode();
            break;
        case R.id.menu_share:
            shareNote();
            break;
        case R.id.menu_send_to_desktop:
            sendToDesktop();
            break;
        case R.id.menu_alert:
            setReminder();
            break;
        case R.id.menu_delete_remind:
            clearAlertDate();
            break;
        case R.id.join_password:
            handleJoinPassword();
            break;
        case R.id.out_password:
            handleOutPassword();
            break;
        default:
            break;
    }
    return true;
}

private void showDeleteConfirmationDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.alert_title_delete));
    builder.setIcon(android.R.drawable.ic_dialog_alert);
    builder.setMessage(getString(R.string.alert_message_delete_note));
    builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
        deleteCurrentNote();
        finish();
    });
    builder.setNegativeButton(android.R.string.cancel, null);
    builder.show();
}
```

#### 问题：**String literals should not be duplicated。(字符串字面量不应重复)**

![problem3](https://i.imgur.com/rN5r3uA.png)

重复的字符串字面量会使重构过程变得复杂且容易出错，因为任何更改都需要在所有出现的地方进行传播。

**例外情况**：为了防止产生一些误报，少于 5 个字符的字面量将被排除在外。

这里的问题是因为在代码中重复使用了相同的字符串字面量   `isLocked`，这会导致多个潜在问题。首先，重复的字符串使得代码维护变得困难，如果需要更改字符串的内容，必须在所有出现的地方进行修改，容易遗漏某处而导致错误。

这里可以将字符串字面量定义为常量

``` java
private static final String PREFS_NOTE_LOCK = "NoteLock";
private static final String KEY_IS_LOCKED = "isLocked";
private static final String PREFS_MY_APP = "MyApp";
```

然后在代码中使用这些常量替代直接使用字符串字面量

``` java
SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NOTE_LOCK, MODE_PRIVATE);
if (sharedPreferences.getBoolean(KEY_IS_LOCKED, false)) {
    // 其他逻辑...
}
```