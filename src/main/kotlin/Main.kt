import kotlin.concurrent.Volatile
import kotlin.reflect.KClass

@DslMarker
annotation class TableBuilderDslMarker

fun table(database: Database, initializer: SqlTableBuilder.() -> Unit) =
    SqlTableBuilder(database).apply(initializer).also {

        val contents = it.build().getColumnData()
        println(contents.keys.joinToString(" "))

        println(
            List(contents.values.size) { index ->
                contents.values.joinToString("\t") { j -> j[index].toString() }
            }.joinToString("\n")
        )
    }

@TableBuilderDslMarker
class SqlTableBuilder(private val database: Database) {

    fun columns(initializer: ColumnBuilder.() -> Unit) {
        ColumnBuilder(database).apply(initializer).build()
    }

    fun row(initializer: EntityBuilder.() -> Unit) {
        EntityBuilder(database).apply(initializer)
    }

    fun build() = database

}

data class SqlTableColumn<T : Any>(val name: String, val clazz: KClass<T>)

@TableBuilderDslMarker
class ColumnBuilder(private val database: Database) {

    private val columnInfo = mutableMapOf<String, SqlTableColumn<*>>()

    fun column(name: String, clazz: KClass<*>) {
        val availableDataTypes = database.getAvailableDataTypes()
        if (clazz !in availableDataTypes) throw RuntimeException("Unsupported data type")
        columnInfo[name] = SqlTableColumn(name, clazz)
    }

    fun build() = database.setTableInfo(columnInfo)

}

@TableBuilderDslMarker
class EntityBuilder(val database: Database) {

    inline fun <reified T : Any> cell(columnName: String, value: T) {
        val type = database.getTableInfo()[columnName]?.clazz
            ?: throw RuntimeException("No such column with name $columnName")
        val columnDataType = (T::class)
        if (type != columnDataType) {
            throw RuntimeException(
                "Incorrect data type. Received ${columnDataType.simpleName}." +
                        " Should be ${type.simpleName}"
            )
        }
        database.insertColumnData(columnName, value)
    }
}


interface Database {
    fun getAvailableDataTypes(): List<KClass<*>>
    fun setTableInfo(columnInfo: Map<String, SqlTableColumn<*>>)
    fun getTableInfo(): Map<String, SqlTableColumn<*>>
    fun insertColumnData(string: String, obj: Any)
    fun getColumnData(): Map<String, List<Any>>
}

class TestDatabaseImpl : Database {

    private var tableInfo: Map<String, SqlTableColumn<*>> = mapOf()

    private var entityInfo: MutableMap<String, MutableList<Any>> = mutableMapOf()

    override fun getAvailableDataTypes(): List<KClass<*>> {
        return listOf(Int::class, String::class, Float::class, Boolean::class)
    }

    override fun setTableInfo(columnInfo: Map<String, SqlTableColumn<*>>) {
        tableInfo = columnInfo.toMap()
    }

    override fun getTableInfo(): Map<String, SqlTableColumn<*>> {
        return tableInfo
    }

    override fun insertColumnData(string: String, obj: Any) {
        entityInfo.getOrPut(string, ::mutableListOf).add(obj)
    }

    override fun getColumnData(): Map<String, List<Any>> {
        return entityInfo
    }
}

/*
 * Supported types are described in Database Implementation
 * Order of columns is not specific
 * Some basic data type checks are being performed
 */

interface Engine {

    fun startUp()

    fun stop()

}

abstract class MechanicalEngine(private val power: UInt) : Engine {

    open fun description() = javaClass.name + power.toString()

    fun getPower() = power

}

class InternalCombustionEngine(
    power: UInt,
    private val flammableFuelType: FlammableFuelType
) : MechanicalEngine(power) {

    enum class FlammableFuelType {
        DIESEL,
        PETROL,
        BENZINE,
        GAS,
    }

    override fun description() = super.description() + flammableFuelType.name

    override fun startUp() = println(description() + " fired up the fuel")

    override fun stop() = println(description() + " stopped fuel intake")

    fun getFuelType() = flammableFuelType

}

class ElectricEngine(
    power: UInt,
    private val rotPerSec: UInt
) : MechanicalEngine(power) {

    override fun description() = super.description() + rotPerSec.toString()

    override fun startUp() = println(description() + " started rotating")

    override fun stop() = println(description() + " stopped rotating")

    fun getRotationPerSecond() = rotPerSec

}

class CompressedAirEngine(
    power: UInt,
    private val gasPressure: UInt
) : MechanicalEngine(power) {

    override fun description() = super.description() + gasPressure.toString()

    override fun startUp() = println(description() + " started pressuring gas")

    override fun stop() = println(description() + " released gas")

    fun getGasPressure() = gasPressure

}

interface IMovable {

    fun move(distance: ULong)

}

abstract class Transport(protected val engine: MechanicalEngine) : IMovable {

    fun description() = javaClass.name + engine.description()

}

sealed class FlyingTransport(engine: MechanicalEngine) : Transport(engine) {

    override fun move(distance: ULong) {
        print("Taking off")
        engine.startUp()
        print("Flying for $distance")
        engine.stop()
        print("Descending")
    }

    class AirPlanner(engine: CompressedAirEngine) : FlyingTransport(engine)

    class AirPlane(engine: InternalCombustionEngine) : FlyingTransport(engine)

    class Jet(engine: ElectricEngine) : FlyingTransport(engine)

}

sealed class GroundTransport(engine: MechanicalEngine) : Transport(engine) {

    override fun move(distance: ULong) {
        print("Setting off")
        engine.startUp()
        print("Driving for $distance")
        engine.stop()
        print("Slowing down")
    }

    class Motorcycle(engine: InternalCombustionEngine) : GroundTransport(engine)

    class ElectricCar(engine: ElectricEngine) : GroundTransport(engine)

}

object Singleton {
    @Volatile
    private var singletonObject: Singleton? = null
    val instance: Singleton?
        get() {
            if (singletonObject == null) {
                synchronized(Singleton::class.java) {
                    if (singletonObject == null) {
                        singletonObject = Singleton
                    }
                }
            }
            return singletonObject
        }
}

fun main() {
    val transportList = listOf("Motorcycle", "ElectricCar", "Jet", "AirPlane", "AirPlanner")
    println("Select transport type: ${transportList.joinToString(", ")}")

    readlnOrNull()?.let { input ->

        if (input !in transportList)
            println("Unknown transport type")

        val transport = when (input) {
            "Motorcycle" -> GroundTransport.Motorcycle(
                engine = InternalCombustionEngine(300U, InternalCombustionEngine.FlammableFuelType.BENZINE)
            )

            "ElectricCar" -> GroundTransport.ElectricCar(
                engine = ElectricEngine(100U, 2000U)
            )

            "Jet" -> FlyingTransport.Jet(
                engine = ElectricEngine(500U, 3000U)
            )

            "AirPlanner" -> FlyingTransport.AirPlanner(
                engine = CompressedAirEngine(300U, 100000U)
            )

            "AirPlane" -> FlyingTransport.AirPlane(
                engine = InternalCombustionEngine(600U, InternalCombustionEngine.FlammableFuelType.GAS)
            )

            else -> {
                return
            }
        }

        println(transport.description())
    }

    val dbImpl = TestDatabaseImpl()
    table(dbImpl) {
        columns {
            column("ID", Int::class)
            column("Name", String::class)
            column("Age", Float::class)
            column("Gender", Boolean::class)
        }
        row {
            cell("ID", 12)
            cell("Name", "Daniil")
            cell("Age", 31.2f)
            cell("Gender", false)
        }
        row {
            cell("ID", 54)
            cell("Name", "Name1")
            cell("Gender", true)
            cell("Age", 16.1f)
        }
        row {
            cell("ID", 12)
            cell("Name", "Hello")
            cell("Age", 31.0f)
            cell("Gender", false)
        }
        row {
            cell("Name", "Kotlin")
            cell("Age", 12.9f)
            cell("ID", 10)
            cell("Gender", true)
        }
    }
}




