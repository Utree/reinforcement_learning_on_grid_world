import java.util.*
import java.io.File

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
// グリッドワールドを初期化
fun MutableMap<Position, Cell>.init(index: Int) {
    for(mapInitI in 0 until index) {
        for(mapInitJ in 0 until index) {
            // up
            val up: Int = if (mapInitI > 0) 0 else -1
            // down
            val down: Int = if (mapInitI < index - 1) 0 else -1
            // right
            val right: Int = if (mapInitJ < index - 1) 0 else -1
            // left
            val left: Int = if (mapInitJ > 0) 0 else -1

            this[Position(mapInitJ, mapInitI)] = Cell(left, up, down, right)
        }
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
class Agent(val grid: World, var movementLog: MutableList<Position>, private val isLearning: Boolean) {
    private val rand = Random()

    /**
     * 次の移動先を決定する
     */
    fun move() {
        // 現在位置
        val currentPosition = movementLog.last()
        // 現在位置の上下左右の矢印の長さを取得
        val arrow: Cell = grid.cells[currentPosition]!!
        // 矢印の長さをlist形式にする
        var arrowList = listOf(arrow.up, arrow.down, arrow.right, arrow.left)
        // 学習なら0, 評価なら矢印の最大値未満の値は選択する確率を0にする
        val baseLine = if(isLearning) {
            0
        } else {
            arrowList.max()
        }
        // 矢印の長さを確率計算用に修正
        val up = if(arrow.up >= baseLine!!) arrow.up + 1 else 0
        val down = if(arrow.down >= baseLine) arrow.down + 1 else 0
        val right = if(arrow.right >= baseLine) arrow.right + 1 else 0
        val left = if(arrow.left >= baseLine) arrow.left + 1 else 0
        // 矢印の長さをlist形式にする(修正)
        arrowList = listOf(up, down, right, left)

        // 確率的に選択
        val choice = rand.nextInt(up + down + right + left)
        // 現在位置を変更
        movementLog.add(
            if(0 <= choice && choice < arrowList[0]) {
                // UP
                Position(currentPosition.x, currentPosition.y-1)
            } else if(arrowList[0] <= choice && choice < arrowList[0] + arrowList[1]) {
                // DOWN
                Position(currentPosition.x, currentPosition.y+1)
            } else if(arrowList[0] + arrowList[1] <= choice && choice < arrowList[0] + arrowList[1] + arrowList[2]) {
                // RIGHT
                Position(currentPosition.x+1, currentPosition.y)
            } else {
                // LEFT
                Position(currentPosition.x-1, currentPosition.y)
            }
        )
    }

    /**
     * movementLogをリセット
     *
     * @param gridWidth グリッドワールドの幅
     *
     * @param goalPosition ゴールの位置
     *  ex)
     *   0  1  2  3
     *   4  5  6  7
     *   8  9 10 11
     *  12 13 14 15
     */
    fun resetPosition(gridWidth: Int, goalPosition: Int) {
        val nextAgentPositions = (0 until gridWidth*gridWidth).toMutableList()
        // ゴールとの重複をなくす
        nextAgentPositions.removeAt(goalPosition)
        // シャッフル
        nextAgentPositions.shuffle()
        // 再設定
        this.movementLog = mutableListOf(Position(nextAgentPositions[0]/gridWidth, nextAgentPositions[0]%gridWidth))
    }
}

/**
 * グリッドワールドの広さ(横幅)
 *
 * 基本パラメータ: 7
 *
 * 考察(1-1)では2〜11までを実験する
 **/
const val gridsMax = 11
const val gridsMin = 2
const val gridsBasic = 7
/**
 * ステップ(移動)回数の上限
 *
 * 基本パラメータ: 20
 *
 * 考察(2-4),(3-3)では1-25までを実験する
 **/
const val stepUpperLimits = 25
const val stepLowerLimits = 1
const val stepBasicLimits = 20
/** 報酬 */
const val reward = 10
/**
 * 学習の繰り返し回数
 *
 * 基本パラメータ: 20
 *
 * 考察(2-5),(3-4)では1-25までを実験する
 **/
const val repeatUpperNumberOfLearning = 25
const val repeatLowerNumberOfLearning = 1
const val repeatBasicNumberOfLearning = 20
/** 忘却 */
const val memoryTUpper = 10
const val memoryTLower = 1
const val memoryTBasic = 7
/** ゴールの基本位置 */
const val goalBasic = 0
/**
 * agentの通し番号
 *
 * 注意: 保存するDBにはデータが入っていないことを確認
 */
var agentCounter = 1

/** 学習結果(Agent)を記録 */
var learningAgents = mutableListOf<List<Int>>()
/** 学習結果(movementLog)を記録 */
var learningMovementLog = mutableListOf<List<Int>>()
/** 学習結果(gridWorld)を記録 */
var learningGridWorld = mutableListOf<List<Int>>()

/** 出力用jsファイル */
val jsFile = File("./out/web/chart_data.js").absoluteFile
/** 環境(1-2) 学習時 結果CSS */
val css12LearningFile = File("./out/web/grid_data_1_2_learning.css").absoluteFile
/** 環境(1-2) 評価時 結果CSS */
val css12EvaluatingFile = File("./out/web/grid_data_1_2_evaluating.css").absoluteFile
/** 評価(3-3) 結果CSS */
val css33File = File("./out/web/grid_data_3_3.css").absoluteFile
/** 評価(3-4) 結果CSS */
val css34File = File("./out/web/grid_data_3_4.css").absoluteFile



/**
 * エントリーポイント
 */
fun main(args: Array<String>) {
    /**
     * 環境(1-1) グリッドワールドの大きさ
     *
     * 学習しやすいグリッドワールドの広さは？
     **/
    println("-- 環境(1-1) --")
    /** 学習 */
    learn(1)
    /** 評価 */
    evaluate11()
    /** クリア */
    agentCounter = 1
    learningAgents.clear()
    learningMovementLog.clear()
    learningGridWorld.clear()

    /**
     * 環境(1-2) ゴールの位置
     *
     * ゴールの位置と学習のしやすさに関係があるか?
     **/
    println("-- 環境(1-2) --")
    /** 学習 */
    learn(2)
    /** 評価 */
    evaluate12()
    /** クリア */
    agentCounter = 1
    learningAgents.clear()
    learningMovementLog.clear()
    learningGridWorld.clear()

    /**
     * 学習(2-3) Tの大きさ
     *
     * 履歴の長さと学習しやすさに関係があるか？
     */
    println("-- 学習(2-3) --")
    /** 学習 */
    learn(3)
    /** 評価 */
    evaluate23()
    /** クリア */
    agentCounter = 1
    learningAgents.clear()
    learningMovementLog.clear()
    learningGridWorld.clear()

    /**
     * 学習(2-4) エージェントの移動距離
     *
     * エージェントの寿命はどのくらいが適切か？
     */

    /**
     * 評価(3-3) エージェントの移動距離
     *
     * 寿命は長い, 短い, 同じのどれがよいか?
     */
    println("-- 学習(2-4) & 評価(3-3) --")
    /** 学習 */
    learn(4)
    /** 評価 */
    evaluate2433()
    /** クリア */
    agentCounter = 1
    learningAgents.clear()
    learningMovementLog.clear()
    learningGridWorld.clear()

    /**
     * 学習(2-5) 学習の繰り返し回数
     *
     * 学習に必要なエージェント数は？
     */

    /**
     * 評価(3-4) 評価の繰り返し回数
     *
     * 少ないときと多いときで傾向が変わるのか?
     */
    println("-- 学習(2-5) & 評価(3-4) --")
    /** 学習 */
    learn(5)
    /** 評価 */
    evaluate2534()
    /** クリア */
    agentCounter = 1
    learningAgents.clear()
    learningMovementLog.clear()
    learningGridWorld.clear()

    print("Hello Kotlin")
}

fun learn(switch: Int) {
    /** グリッドワールドの広さを定義 */
    for (gridWidth in gridsMin..gridsMax) {
        /** ゴール位置を決定 */
        val goalPositions = (0 until gridWidth * gridWidth).toMutableList()
        // 候補の中から無作為に抽出
        goalPositions.shuffle()
        for (goalPosition in goalPositions) {
            /** ゴールのPositionインスタンスを生成 */
            val goal = Position(goalPosition % gridWidth, goalPosition / gridWidth)

            /** 考察時にエージェントの数が100になるように修正 */
            for(adjustment in 1..(when(gridWidth) {
                2 -> 34
                3 -> 13
                4 -> 7
                5 -> 5
                6,7 -> 3
                8,9,10 -> 2
                else -> 1
            })) {
                /** エージェントの初期位置を決定 */
                val agentPositions = (0 until gridWidth * gridWidth).toMutableList()
                // ゴールとの重複をなくす
                agentPositions.removeAt(goalPosition)
                // 候補の中から無作為に抽出
                agentPositions.shuffle()
                for (agentPosition in agentPositions) {
                    /** グリッドワールドを生成 */
                    // iは縦軸, jは横軸
                    val initWorld = World(mutableMapOf())
                    // グリッドワールドを初期化
                    initWorld.cells.init(gridWidth)
                    // エージェントのインスタンスを生成
                    val agent = Agent(initWorld, mutableListOf(Position(agentPosition % gridWidth, agentPosition / gridWidth)), true)


                    /** ステップ回数を決定 */
                    for (step in stepLowerLimits..(if(gridWidth == gridsBasic && goalPosition == goalBasic) {
                        stepUpperLimits
                    } else {
                        stepBasicLimits
                    })) {
                        /** Tの値を決定 */
                        for(paramT in memoryTLower..memoryTUpper) {
                            /** 学習回数を決定 */
                            for (repeatNum in 1..(if(gridWidth == gridsBasic && goalPosition == goalBasic) {
                                repeatUpperNumberOfLearning
                            } else {
                                repeatBasicNumberOfLearning
                            })) {

                                /**
                                 * バリデーション
                                 **/
                                if(when(switch) {
                                            1 -> (goalPosition == goalBasic && repeatNum == repeatBasicNumberOfLearning && step == stepBasicLimits && paramT == memoryTBasic)
                                            2 -> (gridWidth == gridsBasic && repeatNum == repeatBasicNumberOfLearning && step == stepBasicLimits && paramT == memoryTBasic)
                                            3 -> (gridWidth == gridsBasic && goalPosition == goalBasic && repeatNum == repeatBasicNumberOfLearning && step == stepBasicLimits)
                                            4 -> (gridWidth == gridsBasic && goalPosition == goalBasic && repeatNum == repeatBasicNumberOfLearning && paramT == memoryTBasic)
                                            else -> (gridWidth == gridsBasic && goalPosition == goalBasic && step == stepBasicLimits && paramT == memoryTBasic)

                                        }) {
                                    // 成功フラグ
                                    var isSuccess = 0
                                    // ステップ数だけ繰り返す
                                    for(stp in 1..step) {
                                        // エージェントを移動
                                        agent.move()

                                        // ゴール時
                                        if (goal == agent.movementLog.last()) {
                                            // movementLogがTより小さかった時の挙動
                                            val pT = if(agent.movementLog.size-1 < paramT) {
                                                agent.movementLog.size-1
                                            } else {
                                                paramT
                                            }

                                            // 成功フラグを立てる
                                            isSuccess = 1

                                            /**　arrowを計算 */
                                            for (movementLogIndex in agent.movementLog.size-2 downTo 0) {
                                                // ptは1からpTまでの値
                                                val pt = pT - movementLogIndex
                                                // 矢印の長さを定義
                                                val newLength = reward * (pT - pt + 1) / pT

                                                // movementLogの前後関係から移動方向を取得
                                                if (agent.movementLog[movementLogIndex].x < agent.movementLog[movementLogIndex + 1].x) {
                                                    // 右
                                                    agent.grid.cells[Position(agent.movementLog[movementLogIndex].x, agent.movementLog[movementLogIndex].y)]!!.right += newLength
                                                } else if (agent.movementLog[movementLogIndex].x > agent.movementLog[movementLogIndex + 1].x) {
                                                    // 左
                                                    agent.grid.cells[Position(agent.movementLog[movementLogIndex].x, agent.movementLog[movementLogIndex].y)]!!.left += newLength
                                                } else {
                                                    if (agent.movementLog[movementLogIndex].y > agent.movementLog[movementLogIndex + 1].y) {
                                                        // 上
                                                        agent.grid.cells[Position(agent.movementLog[movementLogIndex].x, agent.movementLog[movementLogIndex].y)]!!.up += newLength
                                                    } else {
                                                        // 下
                                                        agent.grid.cells[Position(agent.movementLog[movementLogIndex].x, agent.movementLog[movementLogIndex].y)]!!.down += newLength
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    /** 記録 */
                                    // agentsを記録
                                    learningAgents.add(listOf(agentCounter, gridWidth, goalPosition % gridWidth, goalPosition / gridWidth, repeatNum, step, isSuccess, paramT))
                                    // movement_logを記録
                                    for(sqlMLog in 0 until agent.movementLog.size) {
                                        learningMovementLog.add(listOf(agentCounter, agent.movementLog[sqlMLog].x, agent.movementLog[sqlMLog].y, sqlMLog))
                                    }
                                    // gridWorldを記録
                                    for(cellY in 0 until gridWidth) {
                                        for (cellX in 0 until gridWidth) {
                                            learningGridWorld.add(listOf(agentCounter, cellX, cellY, agent.grid.cells[Position(cellX, cellY)]!!.left, agent.grid.cells[Position(cellX, cellY)]!!.up, agent.grid.cells[Position(cellX, cellY)]!!.down, agent.grid.cells[Position(cellX, cellY)]!!.right))
                                        }
                                    }

                                    /** エージェントの初期位置を再設定 */
                                    agent.resetPosition(gridWidth, goalPosition)
                                    // エージェントの通し番号を更新
                                    agentCounter++
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun evaluate11() {
    /** 評価Agent 統計情報を出力するための一時保存用 */
    var evaluatingAgents: List<List<Int>>
    /** learnedAgentsから100のバリデーションを掛けてsuccessFlagが立ってたもの 統計情報を出力するための一時保存用 */
    var successAgents: List<List<Int>>
    /** 結果出力用 */
    var resultLearningPercentage: List<Int> = mutableListOf()
    var resultEvaluatingPercentage: List<Int> = mutableListOf()

    for (gridWidth in gridsMin..gridsMax) {
        // クリア
        successAgents = mutableListOf()
        evaluatingAgents = mutableListOf()

        /** エージェントを探す */
        for (tmp in learningAgents) {
            /**
             * 1. グリッドの広さ: n
             * 2. ゴールの位置: (0, 0)
             * 3. 学習回数: 20
             * 4. ステップ回数: 20
             * 5. Tの値: 7
             * が一致するものを探す
             */
            if (tmp[1] == gridWidth &&
                    tmp[2] == goalBasic &&
                    tmp[3] == goalBasic &&
                    tmp[4] == repeatBasicNumberOfLearning &&
                    tmp[5] == stepBasicLimits &&
                    tmp[7] == memoryTBasic) {
                evaluatingAgents.add(tmp)
            }
        }

        // シャッフル
        evaluatingAgents.shuffle()

        /** 各グリッドにおける学習の成功確率(100エージェントのバリデーションを掛ける) */
        for (agentIndex in 0 until 100) {
            if (evaluatingAgents[agentIndex][6] == 1) {
                successAgents.add(evaluatingAgents[agentIndex])
            }
        }

        // 評価用に消去
        evaluatingAgents.clear()

        /** 統計情報 */
        println("グリッド幅:$gridWidth における学習の成功確率は ${successAgents.size} %です。")
        resultLearningPercentage += successAgents.size


        /** 評価 */
        // グリッドワールドを生成
        val initWorld = World(mutableMapOf())
        // 初期化
        initWorld.cells.init(gridWidth)
        // arrowを設定
        for (aTmp in successAgents) {
            for (gTmp in learningGridWorld) {
                // agentIdが一致するものを探す
                if (gTmp[0] == aTmp[0]) {
                    initWorld.cells[Position(gTmp[1], gTmp[2])]!!.left += gTmp[3]
                    initWorld.cells[Position(gTmp[1], gTmp[2])]!!.up += gTmp[4]
                    initWorld.cells[Position(gTmp[1], gTmp[2])]!!.down += gTmp[5]
                    initWorld.cells[Position(gTmp[1], gTmp[2])]!!.right += gTmp[6]
                }
            }
        }

        /** 考察時にエージェントの数が100になるように修正 */
        for (adjustment in 1..(when (gridWidth) {
            2 -> 34
            3 -> 13
            4 -> 7
            5 -> 5
            6, 7 -> 3
            8, 9, 10 -> 2
            else -> 1
        })) {
            /** ゴール位置 */
            val goal = Position(goalBasic, goalBasic)
            /** エージェントの初期位置を決定 */
            val agentPositions = (0 until gridWidth * gridWidth).toMutableList()
            // ゴールとの重複をなくす
            agentPositions.removeAt(goalBasic)
            // 候補の中から無作為に抽出
            agentPositions.shuffle()
            // エージェントの初期位置を決定
            for(agentPosition in agentPositions) {
                // エージェントを生成
                val agent = Agent(initWorld, mutableListOf(Position(agentPosition % gridWidth, agentPosition / gridWidth)), false)

                // 評価回数を定義
                for(repeatNum in 1..repeatBasicNumberOfLearning) {
                    // 成功フラグ
                    var isSuccess = 0
                    // ステップ回数を定義
                    for(j in 1..stepBasicLimits) {
                        // 移動
                        agent.move()


                        // ゴール時
                        if (goal == agent.movementLog.last()) {
                            isSuccess = 1
                        }
                    }

                    // 規定の評価回数になったら記録を取る
                    if(repeatNum == repeatBasicNumberOfLearning) {
                        /** 記録 */
                        // agentsを記録
                        evaluatingAgents.add(listOf(agentCounter, gridWidth, goalBasic, goalBasic, repeatNum, stepBasicLimits, isSuccess))
                        // エージェントの通し番号を更新
                        agentCounter++
                    }

                    /** エージェントの初期位置を再設定 */
                    agent.resetPosition(gridWidth, goalBasic)
                }
            }
        }

        // 評価用に消去
        successAgents.clear()
        // シャッフル
        evaluatingAgents.shuffle()

        /** 各グリッドにおける評価の成功確率(100エージェントのバリデーションを掛ける) */
        for (agentIndex in 0 until 100) {
            if (evaluatingAgents[agentIndex][6] == 1) {
                successAgents.add(evaluatingAgents[agentIndex])
            }
        }
        /** 統計情報 */
        println("グリッド幅:$gridWidth における評価の成功確率は ${successAgents.size} %です。")
        println("--------------")
        resultEvaluatingPercentage += successAgents.size
    }

    /** 結果出力 */
    jsFile.appendText("// 環境(1-1) グリッドワールドの大きさ\n")
    jsFile.appendText("var ctx_0_learn_data = $resultLearningPercentage;\n")
    jsFile.appendText("var ctx_0_evaluate_data = $resultEvaluatingPercentage;\n")
}

fun evaluate12() {
    /** 評価Agent 統計情報を出力するための一時保存用 */
    var evaluatingAgents: List<List<Int>>
    /** learnedAgentsから100のバリデーションを掛けてsuccessFlagが立ってたもの 統計情報を出力するための一時保存用 */
    var successAgents: List<List<Int>>

    /** Cell番号を定義 */
    for(cellY in 0 until gridsBasic) {
        for (cellX in 0 until gridsBasic) {
            // クリア
            successAgents = mutableListOf()
            evaluatingAgents = mutableListOf()

            /** エージェントを探す */
            for (tmp in learningAgents) {
                /**
                 * 1. グリッドの広さ: 7
                 * 2. ゴールの位置: n
                 * 3. 学習回数: 20
                 * 4. ステップ回数: 20
                 * 5. Tの値: 7
                 * が一致するものを探す
                 */
                if (tmp[1] == gridsBasic &&
                        tmp[2] == cellX &&
                        tmp[3] == cellY &&
                        tmp[4] == repeatBasicNumberOfLearning &&
                        tmp[5] == stepBasicLimits &&
                        tmp[7] == memoryTBasic) {
                    evaluatingAgents.add(tmp)
                }
            }
            // シャッフル
            evaluatingAgents.shuffle()

            /** 各グリッドにおける学習の成功確率(100エージェントのバリデーションを掛ける) */
            for (agentIndex in 0 until 100) {
                if (evaluatingAgents[agentIndex][6] == 1) {
                    successAgents.add(evaluatingAgents[agentIndex])
                }
            }

            // 評価用に消去
            evaluatingAgents.clear()

            /** 統計情報 */
            css12LearningFile.appendText("#table_1_2_learning .table_data_x_$cellX.table_data_y_$cellY {\n\t" +
                    when(successAgents.size) {
                        in 60..100 -> "background-color: firebrick;"
                        in 50..59 -> "background-color: crimson;"
                        in 40..49 -> "background-color: tomato;"
                        in 30..39 -> "background-color: darkorange;"
                        else -> "background-color: gold;"
                    } +
                    "\n}\n")

            /** 評価 */
            // グリッドワールドを生成
            val initWorld = World(mutableMapOf())
            // 初期化
            initWorld.cells.init(gridsBasic)
            // arrowを設定
            for (aTmp in successAgents) {
                for (gTmp in learningGridWorld) {
                    // agentIdが一致するものを探す
                    if (gTmp[0] == aTmp[0]) {
                        initWorld.cells[Position(gTmp[1], gTmp[2])]!!.left += gTmp[3]
                        initWorld.cells[Position(gTmp[1], gTmp[2])]!!.up += gTmp[4]
                        initWorld.cells[Position(gTmp[1], gTmp[2])]!!.down += gTmp[5]
                        initWorld.cells[Position(gTmp[1], gTmp[2])]!!.right += gTmp[6]
                    }
                }
            }

            /** 考察時にエージェントの数が100になるように修正 */
            for (adjustment in 1..(when (gridsBasic) {
                2 -> 34
                3 -> 13
                4 -> 7
                5 -> 5
                6, 7 -> 3
                8, 9, 10 -> 2
                else -> 1
            })) {
                /** ゴール位置 */
                val goal = Position(cellX, cellY)
                /** エージェントの初期位置を決定 */
                val agentPositions = (0 until gridsBasic * gridsBasic).toMutableList()
                // ゴールとの重複をなくす
                agentPositions.removeAt(cellY*gridsBasic + cellX)
                // 候補の中から無作為に抽出
                agentPositions.shuffle()
                // エージェントの初期位置を決定
                for(agentPosition in agentPositions) {
                    // エージェントを生成
                    val agent = Agent(initWorld, mutableListOf(Position(agentPosition % gridsBasic, agentPosition / gridsBasic)), false)

                    // 評価回数を定義
                    for(repeatNum in 1..repeatBasicNumberOfLearning) {
                        // 成功フラグ
                        var isSuccess = 0
                        // ステップ回数を定義
                        for(j in 1..stepBasicLimits) {
                            // 移動
                            agent.move()


                            // ゴール時
                            if (goal == agent.movementLog.last()) {
                                isSuccess = 1
                            }
                        }

                        // 規定の評価回数になったら記録を取る
                        if(repeatNum == repeatBasicNumberOfLearning) {
                            /** 記録 */
                            // agentsを記録
                            evaluatingAgents.add(listOf(agentCounter, gridsBasic, cellX, cellY, repeatNum, stepBasicLimits, isSuccess))
                            // エージェントの通し番号を更新
                            agentCounter++
                        }

                        /** エージェントの初期位置を再設定 */
                        agent.resetPosition(gridsBasic, cellY*gridsBasic + cellX)
                    }
                }
            }

            // 評価用に消去
            successAgents.clear()
            // シャッフル
            evaluatingAgents.shuffle()

            /** 各グリッドにおける評価の成功確率(100エージェントのバリデーションを掛ける) */
            for (agentIndex in 0 until 100) {
                if (evaluatingAgents[agentIndex][6] == 1) {
                    successAgents.add(evaluatingAgents[agentIndex])
                }
            }
            /** 統計情報 */
            println("($cellX,$cellY) における評価の成功確率は ${successAgents.size} %です。")
            println("--------------")
            css12EvaluatingFile.appendText("#table_1_2_evaluating .table_data_x_$cellX.table_data_y_$cellY {\n\t" +
                    when(successAgents.size) {
                        100 -> "background-color: darkslategray;"
                        in 90..99 -> "background-color: darkgreen;"
                        in 80..89 -> "background-color: forestgreen;"
                        in 70..79 -> "background-color: mediumaquamarine;"
                        else -> "background-color: GreenYellow;"
                    } +
                    "\n}\n")
        }
    }
}

fun evaluate23() {
    /** 評価Agent 統計情報を出力するための一時保存用 */
    var evaluatingAgents: List<List<Int>>
    /** learnedAgentsから100のバリデーションを掛けてsuccessFlagが立ってたもの 統計情報を出力するための一時保存用 */
    var successAgents: List<List<Int>>
    /** 結果出力用 */
    var resultLearningPercentage: List<Int> = mutableListOf()
    var resultEvaluatingPercentage: List<Int> = mutableListOf()

    /** Tの値を定義 */
    for(paramT in memoryTLower..memoryTUpper) {
        // クリア
        successAgents = mutableListOf()
        evaluatingAgents = mutableListOf()

        /** エージェントを探す */
        for (tmp in learningAgents) {
            /**
             * 1. グリッドの広さ: 7
             * 2. ゴールの位置: (0, 0)
             * 3. 学習回数: 20
             * 4. ステップ回数: 20
             * 5. Tの値: n
             * が一致するものを探す
             */
            if (tmp[1] == gridsBasic &&
                    tmp[2] == goalBasic &&
                    tmp[3] == goalBasic &&
                    tmp[4] == repeatBasicNumberOfLearning &&
                    tmp[5] == stepBasicLimits &&
                    tmp[7] == paramT) {
                evaluatingAgents.add(tmp)
            }
        }

        // シャッフル
        evaluatingAgents.shuffle()

        /** 各グリッドにおける学習の成功確率(100エージェントのバリデーションを掛ける) */
        for (agentIndex in 0 until 100) {
            if (evaluatingAgents[agentIndex][6] == 1) {
                successAgents.add(evaluatingAgents[agentIndex])
            }
        }

        // 評価用に消去
        evaluatingAgents.clear()

        /** 統計情報 */
        println("履歴の長さT:$paramT における学習の成功確率は ${successAgents.size} %です。")
        resultLearningPercentage += successAgents.size

        /** 評価 */
        // グリッドワールドを生成
        val initWorld = World(mutableMapOf())
        // 初期化
        initWorld.cells.init(gridsBasic)
        // arrowを設定
        for (aTmp in successAgents) {
            for (gTmp in learningGridWorld) {
                // agentIdが一致するものを探す
                if (gTmp[0] == aTmp[0]) {
                    initWorld.cells[Position(gTmp[1], gTmp[2])]!!.left += gTmp[3]
                    initWorld.cells[Position(gTmp[1], gTmp[2])]!!.up += gTmp[4]
                    initWorld.cells[Position(gTmp[1], gTmp[2])]!!.down += gTmp[5]
                    initWorld.cells[Position(gTmp[1], gTmp[2])]!!.right += gTmp[6]
                }
            }
        }

        // 評価用に消去
        successAgents.clear()

        /** 考察時にエージェントの数が100になるように修正 */
        for (adjustment in 1..(when (gridsBasic) {
            2 -> 34
            3 -> 13
            4 -> 7
            5 -> 5
            6, 7 -> 3
            8, 9, 10 -> 2
            else -> 1
        })) {
            /** ゴール位置 */
            val goal = Position(goalBasic, goalBasic)
            /** エージェントの初期位置を決定 */
            val agentPositions = (0 until gridsBasic * gridsBasic).toMutableList()
            // ゴールとの重複をなくす
            agentPositions.removeAt(goalBasic)
            // 候補の中から無作為に抽出
            agentPositions.shuffle()
            // エージェントの初期位置を決定
            for(agentPosition in agentPositions) {
                // エージェントを生成
                val agent = Agent(initWorld, mutableListOf(Position(agentPosition % gridsBasic, agentPosition / gridsBasic)), false)

                // 評価回数を定義
                for(repeatNum in 1..repeatBasicNumberOfLearning) {
                    // 成功フラグ
                    var isSuccess = 0
                    // ステップ回数を定義
                    for(j in 1..stepBasicLimits) {
                        // 移動
                        agent.move()


                        // ゴール時
                        if (goal == agent.movementLog.last()) {
                            isSuccess = 1
                        }
                    }

                    // 規定の評価回数になったら記録を取る
                    if(repeatNum == repeatBasicNumberOfLearning) {
                        /** 記録 */
                        // agentsを記録
                        evaluatingAgents.add(listOf(agentCounter, gridsBasic, goalBasic, goalBasic, repeatNum, stepBasicLimits, isSuccess))
                        // エージェントの通し番号を更新
                        agentCounter++
                    }

                    /** エージェントの初期位置を再設定 */
                    agent.resetPosition(gridsBasic, goalBasic)
                }
            }
        }

        // シャッフル
        evaluatingAgents.shuffle()

        /** 各グリッドにおける評価の成功確率(100エージェントのバリデーションを掛ける) */
        for (agentIndex in 0 until 100) {
            if (evaluatingAgents[agentIndex][6] == 1) {
                successAgents.add(evaluatingAgents[agentIndex])
            }
        }
        /** 統計情報 */
        println("履歴の長さT:$paramT における評価の成功確率は ${successAgents.size} %です。")
        println("--------------")
        resultEvaluatingPercentage += successAgents.size
    }

    /** 結果出力 */
    jsFile.appendText("// 学習(2-3) Tの大きさ\n")
    jsFile.appendText("var ctx_1_learn_data = $resultLearningPercentage;\n")
    jsFile.appendText("var ctx_1_evaluate_data = $resultEvaluatingPercentage;\n")
}

fun evaluate2433() {
    /** 評価Agent 統計情報を出力するための一時保存用 */
    var evaluatingAgents: List<List<Int>>
    /** learnedAgentsから100のバリデーションを掛けてsuccessFlagが立ってたもの 統計情報を出力するための一時保存用 */
    var successAgents: List<List<Int>>
    /** 結果出力用 */
    var resultLearningPercentage: List<Int> = mutableListOf()

    /** ステップ回数を定義 */
    for(learningStep in stepLowerLimits..stepUpperLimits) {
        // クリア
        successAgents = mutableListOf()
        evaluatingAgents = mutableListOf()

        /** エージェントを探す */
        for (tmp in learningAgents) {
            /**
             * 1. グリッドの広さ: 7
             * 2. ゴールの位置: (0, 0)
             * 3. 学習回数: 20
             * 4. ステップ回数: n
             * 5. Tの値: 7
             * が一致するものを探す
             */
            if (tmp[1] == gridsBasic &&
                    tmp[2] == goalBasic &&
                    tmp[3] == goalBasic &&
                    tmp[4] == repeatBasicNumberOfLearning &&
                    tmp[5] == learningStep &&
                    tmp[7] == memoryTBasic) {
                evaluatingAgents.add(tmp)
            }
        }

        // シャッフル
        evaluatingAgents.shuffle()

        /** 各グリッドにおける学習の成功確率(100エージェントのバリデーションを掛ける) */
        for (agentIndex in 0 until 100) {
            if (evaluatingAgents[agentIndex][6] == 1) {
                successAgents.add(evaluatingAgents[agentIndex])
            }
        }

        // 評価用に消去
        evaluatingAgents.clear()

        /** 統計情報 */
        println("エージェントの寿命:$learningStep における学習の成功確率は ${successAgents.size} %です。")
        resultLearningPercentage += successAgents.size

        /** 評価 */
        // グリッドワールドを生成
        val initWorld = World(mutableMapOf())
        // 初期化
        initWorld.cells.init(gridsBasic)
        // arrowを設定
        for (aTmp in successAgents) {
            for (gTmp in learningGridWorld) {
                // agentIdが一致するものを探す
                if (gTmp[0] == aTmp[0]) {
                    initWorld.cells[Position(gTmp[1], gTmp[2])]!!.left += gTmp[3]
                    initWorld.cells[Position(gTmp[1], gTmp[2])]!!.up += gTmp[4]
                    initWorld.cells[Position(gTmp[1], gTmp[2])]!!.down += gTmp[5]
                    initWorld.cells[Position(gTmp[1], gTmp[2])]!!.right += gTmp[6]
                }
            }
        }

        // 評価用に消去
        successAgents.clear()

        /** 評価のステップ回数を定義*/
        for(evaluateStep in stepLowerLimits..stepUpperLimits) {
            /** 考察時にエージェントの数が100になるように修正 */
            for (adjustment in 1..(when (gridsBasic) {
                2 -> 34
                3 -> 13
                4 -> 7
                5 -> 5
                6, 7 -> 3
                8, 9, 10 -> 2
                else -> 1
            })) {
                /** ゴール位置 */
                val goal = Position(goalBasic, goalBasic)
                /** エージェントの初期位置を決定 */
                val agentPositions = (0 until gridsBasic * gridsBasic).toMutableList()
                // ゴールとの重複をなくす
                agentPositions.removeAt(goalBasic)
                // 候補の中から無作為に抽出
                agentPositions.shuffle()
                // エージェントの初期位置を決定
                for(agentPosition in agentPositions) {
                    // エージェントを生成
                    val agent = Agent(initWorld, mutableListOf(Position(agentPosition % gridsBasic, agentPosition / gridsBasic)), false)

                    // 評価回数を繰り返す
                    for(repeatNum in 1..repeatBasicNumberOfLearning) {
                        // 成功フラグ
                        var isSuccess = 0
                        // ステップ回数を繰り返す
                        for(j in 1..evaluateStep) {
                            // 移動
                            agent.move()


                            // ゴール時
                            if (goal == agent.movementLog.last()) {
                                isSuccess = 1
                            }
                        }

                        // 規定の評価回数になったら記録を取る
                        if(repeatNum == repeatBasicNumberOfLearning) {
                            /** 記録 */
                            // agentsを記録
                            evaluatingAgents.add(listOf(agentCounter, gridsBasic, goalBasic, goalBasic, repeatNum, stepBasicLimits, isSuccess))
                            // エージェントの通し番号を更新
                            agentCounter++
                        }

                        /** エージェントの初期位置を再設定 */
                        agent.resetPosition(gridsBasic, goalBasic)
                    }
                }
            }

            // シャッフル
            evaluatingAgents.shuffle()

            /** 各グリッドにおける評価の成功確率(100エージェントのバリデーションを掛ける) */
            for (agentIndex in 0 until 100) {
                if (evaluatingAgents[agentIndex][6] == 1) {
                    successAgents.add(evaluatingAgents[agentIndex])
                }
            }

            /** 統計情報 */
            println("エージェントの寿命 L:$learningStep E:$evaluateStep における評価の成功確率は ${successAgents.size} %です。")
            println("--------------")
            css33File.appendText("#table_3_3_evaluating .table_data_x_$learningStep.table_data_y_$evaluateStep {\n\t" +
                    when(successAgents.size) {
                        100 -> "background-color: darkslategray;"
                        in 90..99 -> "background-color: darkgreen;"
                        in 80..89 -> "background-color: forestgreen;"
                        in 70..79 -> "background-color: mediumaquamarine;"
                        else -> "background-color: GreenYellow;"
                    } +
                    "\n}\n")

            // 評価用に消去
            evaluatingAgents.clear()
            successAgents.clear()
        }
    }

    /** 結果出力 */
    jsFile.appendText("学習(2-4) エージェントの移動距離\n")
    jsFile.appendText("var ctx_2_learn_data = $resultLearningPercentage;\n")
}

fun evaluate2534() {
    /** 評価Agent 統計情報を出力するための一時保存用 */
    var evaluatingAgents: List<List<Int>>
    /** learnedAgentsから100のバリデーションを掛けてsuccessFlagが立ってたもの 統計情報を出力するための一時保存用 */
    var successAgents: List<List<Int>>
    /** 結果出力用 */
    var resultLearningPercentage: List<Int> = mutableListOf()

    /** 学習の繰り返し回数を定義 */
    for(repeatNumberOfLearning in repeatLowerNumberOfLearning..repeatUpperNumberOfLearning) {
        // クリア
        successAgents = mutableListOf()
        evaluatingAgents = mutableListOf()

        /** エージェントを探す */
        for (tmp in learningAgents) {
            /**
             * 1. グリッドの広さ: 7
             * 2. ゴールの位置: (0, 0)
             * 3. 学習回数: n
             * 4. ステップ回数: 20
             * 5. Tの値: 7
             * が一致するものを探す
             */
            if (tmp[1] == gridsBasic &&
                    tmp[2] == goalBasic &&
                    tmp[3] == goalBasic &&
                    tmp[4] == repeatNumberOfLearning &&
                    tmp[5] == stepBasicLimits &&
                    tmp[7] == memoryTBasic) {
                evaluatingAgents.add(tmp)
            }
        }

        // シャッフル
        evaluatingAgents.shuffle()

        /** 各グリッドにおける学習の成功確率(100エージェントのバリデーションを掛ける) */
        for (agentIndex in 0 until 100) {
            if (evaluatingAgents[agentIndex][6] == 1) {
                successAgents.add(evaluatingAgents[agentIndex])
            }
        }

        // 評価用に消去
        evaluatingAgents.clear()

        /** 統計情報 */
        println("--------------")
        println("学習回数回数:$repeatNumberOfLearning における学習の成功確率は ${successAgents.size} %です。")
        resultLearningPercentage += successAgents.size

        /** 評価 */
        // グリッドワールドを生成
        val initWorld = World(mutableMapOf())
        // 初期化
        initWorld.cells.init(gridsBasic)
        // arrowを設定
        for (aTmp in successAgents) {
            for (gTmp in learningGridWorld) {
                // agentIdが一致するものを探す
                if (gTmp[0] == aTmp[0]) {
                    initWorld.cells[Position(gTmp[1], gTmp[2])]!!.left += gTmp[3]
                    initWorld.cells[Position(gTmp[1], gTmp[2])]!!.up += gTmp[4]
                    initWorld.cells[Position(gTmp[1], gTmp[2])]!!.down += gTmp[5]
                    initWorld.cells[Position(gTmp[1], gTmp[2])]!!.right += gTmp[6]
                }
            }
        }

        // 評価用に消去
        successAgents.clear()

        /** 評価の繰り返し回数を定義 */
        for(repeatNumberOfEvaluate in repeatLowerNumberOfLearning..repeatUpperNumberOfLearning) {
            /** 考察時にエージェントの数が100になるように修正 */
            for (adjustment in 1..(when (gridsBasic) {
                2 -> 34
                3 -> 13
                4 -> 7
                5 -> 5
                6, 7 -> 3
                8, 9, 10 -> 2
                else -> 1
            })) {
                /** ゴール位置 */
                val goal = Position(goalBasic, goalBasic)
                /** エージェントの初期位置を決定 */
                val agentPositions = (0 until gridsBasic * gridsBasic).toMutableList()
                // ゴールとの重複をなくす
                agentPositions.removeAt(goalBasic)
                // 候補の中から無作為に抽出
                agentPositions.shuffle()

                // エージェントの初期位置を決定
                for(agentPosition in agentPositions) {
                    // エージェントを生成
                    val agent = Agent(initWorld, mutableListOf(Position(agentPosition % gridsBasic, agentPosition / gridsBasic)), false)

                    // 評価回数を定義
                    for(repeatNum in 1..repeatNumberOfEvaluate) {
                        // 成功フラグ
                        var isSuccess = 0
                        // ステップ回数を定義
                        for(j in 1..stepBasicLimits) {
                            // 移動
                            agent.move()


                            // ゴール時
                            if (goal == agent.movementLog.last()) {
                                isSuccess = 1
                            }
                        }

                        // 規定の評価回数になったら記録を取る
                        if(repeatNum == repeatNumberOfEvaluate) {
                            /** 記録 */
                            // agentsを記録
                            evaluatingAgents.add(listOf(agentCounter, gridsBasic, goalBasic, goalBasic, repeatNum, stepBasicLimits, isSuccess))
                            // エージェントの通し番号を更新
                            agentCounter++
                        }

                        /** エージェントの初期位置を再設定 */
                        agent.resetPosition(gridsBasic, goalBasic)
                    }
                }
            }

            // シャッフル
            evaluatingAgents.shuffle()

            /** 各グリッドにおける評価の成功確率(100エージェントのバリデーションを掛ける) */
            for (agentIndex in 0 until 100) {
                if (evaluatingAgents[agentIndex][6] == 1) {
                    successAgents.add(evaluatingAgents[agentIndex])
                }
            }
            /** 統計情報 */
            println("繰り返し回数 L:$repeatNumberOfLearning E:$repeatNumberOfEvaluate における評価の成功確率は ${successAgents.size} %です。")
            css34File.appendText("#table_3_4_evaluating .table_data_x_$repeatNumberOfLearning.table_data_y_$repeatNumberOfEvaluate {\n\t" +
                    when(successAgents.size) {
                        100 -> "background-color: darkslategray;"
                        in 90..99 -> "background-color: darkgreen;"
                        in 80..89 -> "background-color: forestgreen;"
                        in 70..79 -> "background-color: mediumaquamarine;"
                        else -> "background-color: GreenYellow;"
                    } +
                    "\n}\n")

            // 評価用に消去
            evaluatingAgents.clear()
            successAgents.clear()
        }
    }

    /** 結果出力 */
    jsFile.appendText("// 学習(2-5) 学習の繰り返し回数\n")
    jsFile.appendText("var ctx_3_learn_data = $resultLearningPercentage;\n")
}