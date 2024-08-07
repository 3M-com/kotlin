/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 4 -> sentence 2
 * NUMBER: 3
 * DESCRIPTION: Real literals with underscores around an exponent mark.
 */

// TESTCASE NUMBER: 1
val value_1 = <!ILLEGAL_UNDERSCORE!>.0__e_0<!>

// TESTCASE NUMBER: 2
val value_2 = <!ILLEGAL_UNDERSCORE!>2.1_e_2<!>

// TESTCASE NUMBER: 3
val value_3 = <!ILLEGAL_UNDERSCORE!>1_E-_2F<!>

// TESTCASE NUMBER: 4
val value_4 = <!ILLEGAL_UNDERSCORE!>3_e+_0f<!>

// TESTCASE NUMBER: 5
val value_5 = <!ILLEGAL_UNDERSCORE!>5_E_<!>-0

// TESTCASE NUMBER: 6
val value_6 = <!ILLEGAL_UNDERSCORE!>5_e_<!>-0F

// TESTCASE NUMBER: 7
val value_7 = <!ILLEGAL_UNDERSCORE!>5_e_00000000<!>

// TESTCASE NUMBER: 8
val value_8 = <!ILLEGAL_UNDERSCORE!>0.0_0___E____<!>-0__0_0F

// TESTCASE NUMBER: 9
val value_9 = <!ILLEGAL_UNDERSCORE!>0__0.0_____e___0f<!>

// TESTCASE NUMBER: 10
val value_10 = <!ILLEGAL_UNDERSCORE!>.0_0_E__0_0<!>

// TESTCASE NUMBER: 11
val value_11 = <!ILLEGAL_UNDERSCORE!>00_______________00.0_0__e+__0_0<!>

// TESTCASE NUMBER: 12
val value_12 = <!ILLEGAL_UNDERSCORE!>33__3.0_e_10__0<!>

// TESTCASE NUMBER: 13
val value_13 = <!ILLEGAL_UNDERSCORE!>.0_E_0______00F<!>

// TESTCASE NUMBER: 14
val value_14 = <!ILLEGAL_UNDERSCORE!>5_________555_________5.0____e____<!>-<!UNRESOLVED_REFERENCE!>____9<!>

// TESTCASE NUMBER: 15
val value_15 = <!ILLEGAL_UNDERSCORE!>666_666.0__________________________________________________1_E+_2___________________________________________________________________0F<!>

// TESTCASE NUMBER: 16
val value_16 = <!ILLEGAL_UNDERSCORE!>9_______9______9_____9____9___9__9_9.0___E__<!>-1

// TESTCASE NUMBER: 17
val value_17 = <!ILLEGAL_UNDERSCORE!>0_0_0_0_0_0_0_0_0_0.12345678___e+__90F<!>

// TESTCASE NUMBER: 18
val value_18 = <!ILLEGAL_UNDERSCORE!>1_2_3_4_5_6_7_8_9.2_3_4_5_6_7_8_9_e_<!>-<!UNRESOLVED_REFERENCE!>_0<!>

// TESTCASE NUMBER: 19
val value_19 = <!ILLEGAL_UNDERSCORE!>.45_6_E_7f<!>

// TESTCASE NUMBER: 20
val value_20 = <!ILLEGAL_UNDERSCORE!>456.5__e_0_6<!>

// TESTCASE NUMBER: 21
val value_21 = <!ILLEGAL_UNDERSCORE!>6_54.76_5_e-_4<!>

// TESTCASE NUMBER: 22
val value_22 = <!ILLEGAL_UNDERSCORE!>7_6543.8____E____7654_3<!>

// TESTCASE NUMBER: 23
val value_23 = <!ILLEGAL_UNDERSCORE!>9_____________87654321.0__e__<!>-9_8765432_____________1F

// TESTCASE NUMBER: 24
val value_24 = <!ILLEGAL_UNDERSCORE!>000000000000000000000000000000000000000000000000000000000000000000000000000000000000000___0.000000000000000000000000_e_000000000000000000000000000000000000000000000000000000000000000_0F<!>

// TESTCASE NUMBER: 25
val value_25 = <!ILLEGAL_UNDERSCORE!>0_000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0_E-_0___000000000000000000000000000000000000000000000000000000000000000000000000000000000000000<!>

// TESTCASE NUMBER: 26
val value_26 = <!ILLEGAL_UNDERSCORE!>9999999999999999999999999999999999999999999_______________999999999999999999999999999999999999999999999.33333333333333333333333333333333333333333333333_333333333333333333333333333333333333333_e_3_3f<!>
