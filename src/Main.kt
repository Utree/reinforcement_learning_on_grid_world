import java.util.*

/**
 *  gridのcell
 *
 *  @param up
 *  上向き矢印の長さ(移動不可の場合は-1)
 *
 *  @param down
 *  下向き矢印の長さ(移動不可の場合は-1)
 *
 *  @param right
 *  右向き矢印の長さ(移動不可の場合は-1)
 *
 *  @param left
 *  左向き矢印の長さ(移動不可の場合は-1)
 *
 *  @param cellPosition
 *  cellの位置
 */
data class Cell(
    var left: Int,
    var up: Int,
    var down: Int,
    var right: Int

) {
    // デバッグ用
    override fun toString(): String {
        print("Cell($left, $up, $down, $right)")
        return ""
    }
}

/**
 *  座標
 *
 *  @property x
 *  x座標
 *
 *  @property y
 *  y座標
 */
data class Position(var x: Int, var y: Int)

/**
 * グリッドワールドのマップ
 *
 * @property cells
 * グリッドワールドのマップチップ
 */
data class World(var cells: MutableMap<Position, Cell>)

/**
 * エージェント
 *
 * @property grid
 * グリッドワールドの矢印長さの情報を持つ
 *
 * @property movementLog
 * 移動ログ
 */
data class Agent(val grid: World, var movementLog: Array<Position>) {
    // Javaデコンパイルすると配列の比較ができなくなるので対処
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(javaClass != other?.javaClass) return false
        other as Agent
        if(!Arrays.equals(movementLog, other.movementLog)) return false
        return true
    }
    // Javaデコンパイルすると同じ値でもhash値が変わってしまうので対処
    override fun hashCode(): Int {
        return grid.hashCode() + Arrays.hashCode(movementLog)
    }
}

/**
 * エントリーポイント
 */
fun main(args: Array<String>) {
    /** グリッドワールドの広さ */
    // Todo: 標準入力からデータをもらう
    var grids = 3

    /** ゴールの位置 */
    // Todo: 動的に決定する
    var goal = Position(0, 1)

    /** エージェントの個数 */
    // Todo: 標準入力からデータをもらう
    var agentNumber = 2

    /** エージェントの初期位置 */
    // Todo: 動的に決定する
    val agentInitPosition1 = Position(2, 1)
    val agentInitPosition2 = Position(1, 1)

    /** ステップ(移動)回数の上限 */
    // Todo: 標準入力からデータをもらう
    var stepUpperLimit = 6

    /** 学習の繰り返し回数 */
    // Todo: 標準入力からデータをもらう
    var repeatNumberOfLearning = 4

    /** エージェント間で報酬を共有するかのフラグ */
    // Todo: 標準入力からデータをもらう
    var shareRewards = true

    /** 学習を繰り返すたびにエージェントの位置を変更するかのフラグ */
    // Todo: 標準入力からデータをもらう
    var changeAgentPosition = true

    /** グリッドワールドを生成 */
    // iは縦軸, jは横軸
    var initWorld = World(mutableMapOf())
    for(i in 0 until grids) {
        for (j in 0 until grids) {
            // up
            val up: Int = if (i > 0) 0 else -1
            // down
            val down: Int = if (i < grids - 1) 0 else -1
            // right
            val right: Int = if (j < grids - 1) 0 else -1
            // left
            val left: Int = if (j > 0) 0 else -1

            initWorld.cells[Position(j, i)] = Cell(left, up, down, right)
        }
    }

    // 表示
    for(i in 0 until grids)
        for(j in 0 until grids) {
            println("${Position(j, i)}: ")
            println("${initWorld.cells[Position(j, i)]} \n")
        }

    /** agentのインスタンスを生成 */
    var agent1 = Agent(initWorld, arrayOf(agentInitPosition1))
    var agent2 = Agent(initWorld, arrayOf(agentInitPosition2))

    //

    print("Hello Kotlin")
}