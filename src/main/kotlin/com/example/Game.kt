package com.example

import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.control.Button
import javafx.stage.Stage
import javafx.util.Duration

class Game : Application() {
    private val cellSize = 10
    private val gridWidth = 60
    private val gridHeight = 40
    private var grid = Array(gridWidth) { BooleanArray(gridHeight) }
    private var running = false
    private var timeline: Timeline? = null
    private lateinit var canvas: Canvas // Store a reference to the canvas

    override fun start(primaryStage: Stage) {
        canvas = Canvas((gridWidth * cellSize).toDouble(), (gridHeight * cellSize).toDouble()) // Initialize canvas
        val gc = canvas.graphicsContext2D

        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED) { event -> handleMouseClick(event, canvas) }

        val startButton = Button("Start").apply { setOnAction { startSimulation() } }
        val stopButton = Button("Stop").apply { setOnAction { stopSimulation() } }
        val resetButton = Button("Reset").apply { setOnAction { resetGrid(canvas) } }

        val controls = HBox(10.0, startButton, stopButton, resetButton)
        val root = BorderPane().apply {
            center = canvas
            bottom = controls
        }

        primaryStage.apply {
            title = "Game of Life - Kotlin"
            scene = Scene(root)
            show()
        }

        drawGrid(gc)
    }

    private fun handleMouseClick(event: MouseEvent, canvas: Canvas) {
        val x = (event.x / cellSize).toInt()
        val y = (event.y / cellSize).toInt()
        if (x in 0 until gridWidth && y in 0 until gridHeight) {
            grid[x][y] = !grid[x][y]
            drawGrid(canvas.graphicsContext2D)
        }
    }

    private fun startSimulation() {
        if (running) return
        running = true
        timeline = Timeline(
            KeyFrame(Duration.millis(200.0), { updateGrid() }) // ✅ Explicitly wrap lambda
        ).apply {
            cycleCount = Timeline.INDEFINITE
            play()
        }
    }

    private fun stopSimulation() {
        running = false
        timeline?.stop()
    }

    private fun resetGrid(canvas: Canvas) {
        stopSimulation()
        grid = Array(gridWidth) { BooleanArray(gridHeight) }
        drawGrid(canvas.graphicsContext2D)
    }

    private fun updateGrid() {
        val newGrid = Array(gridWidth) { BooleanArray(gridHeight) }

        for (x in 0 until gridWidth) {
            for (y in 0 until gridHeight) {
                val neighbors = countAliveNeighbors(x, y)
                newGrid[x][y] = if (grid[x][y]) (neighbors == 2 || neighbors == 3) else (neighbors == 3)
            }
        }

        grid = newGrid
        drawGrid(canvas.graphicsContext2D) // Use the stored canvas reference
    }

    private fun countAliveNeighbors(x: Int, y: Int): Int {
        return (-1..1).sumOf { dx ->
            (-1..1).count { dy ->
                if (dx == 0 && dy == 0) false
                else {
                    val nx = x + dx
                    val ny = y + dy
                    nx in 0 until gridWidth && ny in 0 until gridHeight && grid[nx][ny]
                }
            }
        }
    }

    private fun drawGrid(gc: GraphicsContext) {
        gc.clearRect(0.0, 0.0, (gridWidth * cellSize).toDouble(), (gridHeight * cellSize).toDouble())
        for (x in 0 until gridWidth) {
            for (y in 0 until gridHeight) {
                if (grid[x][y]) {
                    gc.fill = Color.BLACK
                    gc.fillRect(
                        (x * cellSize).toDouble(),
                        (y * cellSize).toDouble(),
                        cellSize.toDouble(),
                        cellSize.toDouble()
                    )
                }
                gc.stroke = Color.LIGHTGRAY
                gc.strokeRect(
                    (x * cellSize).toDouble(),
                    (y * cellSize).toDouble(),
                    cellSize.toDouble(),
                    cellSize.toDouble()
                )
            }
        }
    }
}
