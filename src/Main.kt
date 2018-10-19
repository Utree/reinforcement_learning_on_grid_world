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
data class World(var cells: Array<Array<Cell>>) {
    // Javaデコンパイルすると配列の比較ができなくなるので対処
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(javaClass != other?.javaClass) return false
        other as World
        if(!Arrays.equals(cells, other.cells)) return false
        return true
    }
    // Javaデコンパイルすると同じ値でもhash値が変わってしまうので対処
    override fun hashCode(): Int {
        return Arrays.hashCode(cells)
    }
    // デバッグ用
    override fun toString(): String {
        for(i in cells) {
            for(j in i) {
                print("Cell(${j.left}, ${j.up}, ${j.down}, ${j.right})")
            }
        }
        return ""
    }
}

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
    var shareRewardsFlag = true

    /** 学習を繰り返すたびにエージェントの位置を変更するかのフラグ */
    // Todo: 標準入力からデータをもらう
    var changeAgentPositionFlag = true

    /** グリッドワールドをつくる */
    var initWorld = World(arrayOf(emptyArray()))
    // iは縦軸, jは横軸
    for(i in 0 until grids) {
        var tmpCell = emptyArray<Cell>()
        for(j in 0 until grids) {
            // up
            val up: Int = if(i > 0) 0 else -1
            // down
            val down: Int = if(i < grids-1) 0 else -1
            // right
            val right: Int = if(j < grids-1) 0 else -1
            // left
            val left: Int = if(j > 0) 0 else -1

            tmpCell += Cell(left, up, down, right)
        }
        initWorld.cells += tmpCell
    }

    /** agentのインスタンス */
    var agent1 = Agent(initWorld, arrayOf(agentInitPosition1))
    var agent2 = Agent(initWorld, arrayOf(agentInitPosition2))

    print("Hello Kotlin")
}