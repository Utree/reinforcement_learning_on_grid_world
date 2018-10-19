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
 */
data class Cell(
    var left: Int,
    var up: Int,
    var down: Int,
    var right: Int
)

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
 * _|0_x___
 * 0|
 * y|
 *  |
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
data class Agent(val grid: World, var movementLog: List<Position>)

/**
 * エントリーポイント
 */
fun main(args: Array<String>) {
    /** 乱数生成用インスタンス */
    val rand = Random()

    /** グリッドワールドの広さ */
    // Todo: 標準入力からデータをもらう
    val grids = 3

    /** ゴールとエージェントの位置を決定する */

    /** ゴールの位置 */
    // Todo: 動的に決定する
    val goal = Position(0, 1)

    /** エージェントの個数 */
    // Todo: 標準入力からデータをもらう
    var agentNumber = 2

    /** エージェントの初期位置 */
    // Todo: 動的に決定する
    val agentInitPosition1 = Position(1, 2)
    val agentInitPosition2 = Position(1, 1)

    /** ステップ(移動)回数の上限 */
    // Todo: 標準入力からデータをもらう
    val stepUpperLimit = 6

    /** 学習の繰り返し回数 */
    // Todo: 標準入力からデータをもらう
    val repeatNumberOfLearning = 4

    /** エージェント間で報酬を共有するかのフラグ */
    // Todo: 標準入力からデータをもらう
    val shareRewards = true

    /** 学習を繰り返すたびにエージェントの位置を変更するかのフラグ */
    // Todo: 標準入力からデータをもらう
    val changeAgentPosition = true



    /** グリッドワールドを生成 */
    // iは縦軸, jは横軸
    val initWorld = World(mutableMapOf())
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
    /** agentのインスタンスを生成 */
    val agent1 = Agent(initWorld, listOf(agentInitPosition1))
    val agent2 = Agent(initWorld, listOf(agentInitPosition2))

    /**
     * エージェントを動かす
     */
    // 学習を繰り返す
    for(i in 0 until repeatNumberOfLearning) {
        // エージェントを移動
        for(j in 0 until stepUpperLimit) {
            // 移動先を決定する
            var list1: List<Int> = emptyList()
            // 現在位置
            val currentPosition = agent1.movementLog.last()
            // 現在位置の上下左右の矢印の長さを取得する
            val arrow = agent1.grid.cells[currentPosition]
            // 確率を考慮してステップ先を選択する
            for (k in 0..arrow?.up!!)
                list1 += 0
            for (k in 0..arrow.down)
                list1 += 1
            for (k in 0..arrow.right)
                list1 += 2
            for (k in 0..arrow.left)
                list1 += 3

            // 現在位置を変更する
            agent1.movementLog +=  when(list1[rand.nextInt(list1.size)]) {
                // UP
                0 -> {Position(currentPosition.x, currentPosition.y-1)}
                // DOWN
                1 -> {Position(currentPosition.x, currentPosition.y+1)}
                // RIGHT
                2 -> {Position(currentPosition.x+1, currentPosition.y)}
                // LEFT
                else -> {Position(currentPosition.x-1, currentPosition.y)}
            }

            // Debug
            println("j: $j")
            println(agent1.movementLog)
            println("\n")

            // ゴール後、終了
            if(goal == agent1.movementLog.last()) {
                println("GOAL!!")
                break
            }
        }

        // arrowを計算

        // 移動ログをDBに記録

        // movementLogをリセット
        agent1.movementLog = if(changeAgentPosition) {
            // Todo: エージェントの初期位置を変更
            listOf(agentInitPosition1)
        } else {
            listOf(agentInitPosition1)
        }


        // グリッドワールドを更新
        if(shareRewards) {
            // Todo: エージェント間で報酬を共有
        } else {
        }
    }

    print("Hello Kotlin")
}