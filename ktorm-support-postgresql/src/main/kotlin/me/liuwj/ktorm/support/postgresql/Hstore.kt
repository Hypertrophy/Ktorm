package me.liuwj.ktorm.support.postgresql

import me.liuwj.ktorm.expression.ArgumentExpression
import me.liuwj.ktorm.expression.ScalarExpression
import me.liuwj.ktorm.schema.BooleanSqlType
import me.liuwj.ktorm.schema.ColumnDeclaring
import me.liuwj.ktorm.schema.SqlType
import me.liuwj.ktorm.schema.VarcharSqlType

typealias Hstore = Map<String, String?>
typealias TextArray = Array<String?>

sealed class HstoreExpressionType<RightT : Any, ReturnT : Any>(
    val rightSqlType: SqlType<RightT>,
    val returnSqlType: SqlType<ReturnT>,
    val operator: String
)
object GetValueForKey : HstoreExpressionType<String, String>(VarcharSqlType, VarcharSqlType, "->")
object GetValuesForKey : HstoreExpressionType<TextArray, TextArray>(TextArraySqlType, TextArraySqlType, "->")
object Concatenate : HstoreExpressionType<Hstore, Hstore>(HstoreSqlType, HstoreSqlType, "||")
object ContainsKey : HstoreExpressionType<String, Boolean>(VarcharSqlType, BooleanSqlType, "??")
object ContainsAllKeys : HstoreExpressionType<TextArray, Boolean>(TextArraySqlType, BooleanSqlType, "??&")
object ContainsAnyKeys : HstoreExpressionType<TextArray, Boolean>(TextArraySqlType, BooleanSqlType, "??|")
object Contains : HstoreExpressionType<Hstore, Boolean>(HstoreSqlType, BooleanSqlType, "@>")
object ContainedIn : HstoreExpressionType<Hstore, Boolean>(HstoreSqlType, BooleanSqlType, "<@")
object DeleteKey : HstoreExpressionType<String, Hstore>(VarcharSqlType, HstoreSqlType, "-")
object DeleteKeys : HstoreExpressionType<TextArray, Hstore>(TextArraySqlType, HstoreSqlType, "-")
object DeleteMatchingPairs : HstoreExpressionType<Hstore, Hstore>(HstoreSqlType, HstoreSqlType, "-")

/**
 * Binary expression generic class for all binary operations on `hstore` types
 *
 * @property expressionType The [HstoreExpressionType] that represents this operation
 * @property left the expression's left operand.
 * @property right the expression's right operand.
 */
data class HstoreBinaryExpression<RightT : Any, ReturnT : Any>(
    val expressionType: HstoreExpressionType<RightT, ReturnT>,
    override val left: ScalarExpression<Hstore>,
    override val right: ScalarExpression<RightT>,
    override val isLeafNode: Boolean = false,
    override val extraProperties: Map<String, Any> = emptyMap()
) : BinaryExpression<Hstore, RightT, ReturnT>() {
    override val sqlType: SqlType<ReturnT> = expressionType.returnSqlType
    override val operator: String = expressionType.operator
    override fun copyWithNewOperands(left: ScalarExpression<Hstore>, right: ScalarExpression<RightT>) = copy(left = left, right = right)
}

/**
 * Hstore getValue operator, translated to the -> operator in PostgreSQL.
 */
infix fun ColumnDeclaring<Hstore>.getValue(expr: ColumnDeclaring<String>): HstoreBinaryExpression<String, String> {
    return HstoreBinaryExpression(GetValueForKey, asExpression(), expr.asExpression())
}

/**
 * Hstore get value for key operator, translated to the -> operator in PostgreSQL.
 */
infix fun ColumnDeclaring<Hstore>.getValue(value: String): HstoreBinaryExpression<String, String> {
    return this getValue ArgumentExpression(value, VarcharSqlType)
}

/**
 * Hstore get values for keys for key operator, translated to the -> operator in PostgreSQL.
 */
infix fun ColumnDeclaring<Hstore>.getValues(expr: ColumnDeclaring<TextArray>): HstoreBinaryExpression<TextArray, TextArray> {
    return HstoreBinaryExpression(GetValuesForKey, asExpression(), expr.asExpression())
}

/**
 * Hstore get values for keys operator, translated to the -> operator in PostgreSQL.
 */
infix fun ColumnDeclaring<Hstore>.getValues(value: TextArray): HstoreBinaryExpression<TextArray, TextArray> {
    return this getValues ArgumentExpression(value, TextArraySqlType)
}

/**
 * Hstore concatenate operator, translated to the || operator in PostgreSQL.
 */
infix fun ColumnDeclaring<Hstore>.concat(expr: ColumnDeclaring<Hstore>): HstoreBinaryExpression<Hstore, Hstore> {
    return HstoreBinaryExpression(Concatenate, asExpression(), expr.asExpression())
}

/**
 * Hstore concatenate operator, translated to the || operator in PostgreSQL.
 */
infix fun ColumnDeclaring<Hstore>.concat(value: Hstore): HstoreBinaryExpression<Hstore, Hstore> {
    return this concat ArgumentExpression(value, HstoreSqlType)
}

/**
 * Hstore contains key operator, translated to the ? operator in PostgreSQL.
 */
infix fun ColumnDeclaring<Hstore>.containsKey(expr: ColumnDeclaring<String>): HstoreBinaryExpression<String, Boolean> {
    return HstoreBinaryExpression(ContainsKey, asExpression(), expr.asExpression())
}

/**
 * Hstore contains key operator, translated to the ? operator in PostgreSQL.
 */
infix fun ColumnDeclaring<Hstore>.containsKey(value: String): HstoreBinaryExpression<String, Boolean> {
    return this containsKey ArgumentExpression(value, VarcharSqlType)
}

/**
 * Hstore contains all keys operator, translated to the ?& operator in PostgreSQL.
 */
infix fun ColumnDeclaring<Hstore>.containsAll(expr: ColumnDeclaring<TextArray>): HstoreBinaryExpression<TextArray, Boolean> {
    return HstoreBinaryExpression(ContainsAllKeys, asExpression(), expr.asExpression())
}

/**
 * Hstore contains all keys operator, translated to the ?& operator in PostgreSQL.
 */
infix fun ColumnDeclaring<Hstore>.containsAll(value: TextArray): HstoreBinaryExpression<TextArray, Boolean> {
    return this containsAll ArgumentExpression(value, TextArraySqlType)
}

/**
 * Hstore contains any keys operator, translated to the ?| operator in PostgreSQL.
 */
infix fun ColumnDeclaring<Hstore>.containsAny(expr: ColumnDeclaring<TextArray>): HstoreBinaryExpression<TextArray, Boolean> {
    return HstoreBinaryExpression(ContainsAnyKeys, asExpression(), expr.asExpression())
}

/**
 * Hstore contains any keys operator, translated to the ?| operator in PostgreSQL.
 */
infix fun ColumnDeclaring<Hstore>.containsAny(value: TextArray): HstoreBinaryExpression<TextArray, Boolean> {
    return this containsAny ArgumentExpression(value, TextArraySqlType)
}

/**
 * Hstore contains operator, translated to the @> operator in PostgreSQL.
 */
infix fun ColumnDeclaring<Hstore>.contains(expr: ColumnDeclaring<Hstore>): HstoreBinaryExpression<Hstore, Boolean> {
    return HstoreBinaryExpression(Contains, asExpression(), expr.asExpression())
}

/**
 * Hstore contains operator, translated to the @> operator in PostgreSQL.
 */
infix fun ColumnDeclaring<Hstore>.contains(value: Hstore): HstoreBinaryExpression<Hstore, Boolean> {
    return this contains ArgumentExpression(value, HstoreSqlType)
}

/**
 * Hstore contained in operator, translated to the <@ operator in PostgreSQL.
 */
infix fun ColumnDeclaring<Hstore>.containedIn(expr: ColumnDeclaring<Hstore>): HstoreBinaryExpression<Hstore, Boolean> {
    return HstoreBinaryExpression(ContainedIn, asExpression(), expr.asExpression())
}

/**
 * Hstore contained in operator, translated to the <@ operator in PostgreSQL.
 */
infix fun ColumnDeclaring<Hstore>.containedIn(value: Hstore): HstoreBinaryExpression<Hstore, Boolean> {
    return this containedIn ArgumentExpression(value, HstoreSqlType)
}

/**
 * Hstore delete key operator, translated to the - operator in PostgreSQL.
 */
infix fun ColumnDeclaring<Hstore>.delKey(expr: ColumnDeclaring<String>): HstoreBinaryExpression<String, Hstore> {
    return HstoreBinaryExpression(DeleteKey, asExpression(), expr.asExpression())
}

/**
 * Hstore delete key operator, translated to the - operator in PostgreSQL.
 */
infix fun ColumnDeclaring<Hstore>.delKey(value: String): HstoreBinaryExpression<String, Hstore> {
    return this delKey ArgumentExpression(value, VarcharSqlType)
}

/**
 * Hstore delete keys operator, translated to the - operator in PostgreSQL.
 */
infix fun ColumnDeclaring<Hstore>.delKeys(expr: ColumnDeclaring<TextArray>): HstoreBinaryExpression<TextArray, Hstore> {
    return HstoreBinaryExpression(DeleteKeys, asExpression(), expr.asExpression())
}

/**
 * Hstore delete keys operator, translated to the - operator in PostgreSQL.
 */
infix fun ColumnDeclaring<Hstore>.delKeys(value: TextArray): HstoreBinaryExpression<TextArray, Hstore> {
    return this delKeys ArgumentExpression(value, TextArraySqlType)
}

/**
 * Hstore delete matching pairs operator, translated to the - operator in PostgreSQL.
 */
infix fun ColumnDeclaring<Hstore>.delMatching(expr: ColumnDeclaring<Hstore>): HstoreBinaryExpression<Hstore, Hstore> {
    return HstoreBinaryExpression(DeleteMatchingPairs, asExpression(), expr.asExpression())
}

/**
 * Hstore delete matching pairs operator, translated to the - operator in PostgreSQL.
 */
infix fun ColumnDeclaring<Hstore>.delMatching(value: Hstore): HstoreBinaryExpression<Hstore, Hstore> {
    return this delMatching ArgumentExpression(value, HstoreSqlType)
}