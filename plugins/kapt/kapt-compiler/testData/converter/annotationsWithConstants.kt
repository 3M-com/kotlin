// FILE: lib/R.java
package lib;

public class R {
    public static class id {
        public final static int textView = 100;
    }
}

// FILE: app/R.java
package app;

public class R {
    public static class layout {
        public final static int mainActivity = 100;
    }
}

// FILE: app/R2.java
package app;

public class R2 { // For ButterKnife library project support
    public static class layout {
        public final static int mainActivity = 100;
    }
}

// FILE: app/B.java
package app;

public class B {
    public static class id {
        public final static int textView = 200;
    }

    public final static boolean a1 = false;
    public final static byte a2 = 1;
    public final static int a3 = 2;
    public final static short a4 = 3;
    public final static long a5 = 4L;
    public final static char a6 = '5';
    public final static float a7 = 6.0f;
    public final static double a8 = 7.0;
    public final static String a9 = "A";
}

// FILE: lib/OnClick.java
package lib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnClick {
    int[] value() default {};
}

// FILE: test.kt
package app

import lib.R as LibR
import lib.R.id.textView
import lib.OnClick

annotation class Bind(val id: Int)

annotation class MultiValue(val ids: IntArray)
annotation class MultiValueString(val ids: Array<String>)
annotation class MultiValueByte(val ids: ByteArray)

@Target(AnnotationTarget.FIELD)
annotation class BindField(val id: Int)

annotation class Anno(
        val a1: Boolean,
        val a2: Byte,
        val a3: Int,
        val a4: Short,
        val a5: Long,
        val a6: Char,
        val a7: Float,
        val a8: Double,
        val a9: String)

class MyActivity(
    val param1: Int = B.id.textView,
    var param2: Int = B.a3,
) {
    @Bind(LibR.id.textView)
    @BindField(LibR.id.textView)
    val a = 0

    @Bind(lib.R.id.textView)
    @BindField(lib.R.id.textView)
    val b = 0

    @Bind(app.R.layout.mainActivity)
    @BindField(app.R.layout.mainActivity)
    val c = 0

    @Bind(R.layout.mainActivity)
    @BindField(R.layout.mainActivity)
    val d = 0

    @Bind(R2.layout.mainActivity)
    @BindField(R2.layout.mainActivity)
    @Anno(a1 = B.a1, a2 = B.a2, a3 = B.a3, a4 = B.a4, a5 = B.a5, a6 = B.a6, a7 = B.a7, a8 = B.a8, a9 = B.a9)
    val e = 0

    @Bind(B.id.textView)
    @BindField(B.id.textView)
    val f = 0

    @Bind(LibR.id.textView)
    fun foo() {}

    @Bind(lib.R.id.textView)
    fun foo2() {}

    @Bind(app.R.layout.mainActivity)
    fun foo3() {}

    @Bind(R.layout.mainActivity)
    fun foo4() {}

    @Bind(R2.layout.mainActivity)
    @Anno(a1 = B.a1, a2 = B.a2, a3 = B.a3, a4 = B.a4, a5 = B.a5, a6 = B.a6, a7 = B.a7, a8 = B.a8, a9 = B.a9)
    fun foo5() {}

    @Bind(B.id.textView)
    fun plainIntConstant() {}

    @MultiValue(ids = [])
    fun multi0() {}

    @MultiValue(ids = [B.id.textView])
    fun multi1() {}

    @MultiValue(ids = [B.id.textView, B.a3])
    fun multi2() {}

    @MultiValue(ids = intArrayOf(B.id.textView, B.a3))
    fun multi3() {}

    @MultiValueString(ids = arrayOf(B.a9))
    fun multi4() {}

    @MultiValueByte(ids = byteArrayOf(B.a2))
    fun multi5() {}

    @OnClick(B.id.textView)
    fun multiJava1() {}

    @OnClick(B.id.textView, app.R.layout.mainActivity)
    fun multiJava2() {}

    const val propA = B.id.textView
    val propB = B.id.textView
    var propC = B.id.textView
    @JvmField
    val propD = B.id.textView
    @JvmField
    var propE = B.id.textView
    val propF = JJ.b.length
}

object JJ {
    val b = c()
    fun c() = "42"
}
