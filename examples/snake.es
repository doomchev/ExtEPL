tiles = Texture("tiles.png").cut(8, 3);

// params

width = 64;
height = 48;
food = 10;
appleFood = 5;

// tile numbers

floor = 0;
wall = 1;
apple = 2;
horizontal = 3;
vertical = 4;
leftDown = 5;
leftUp = 6;
rightDown = 7;
rightUp = 8;

// dxdy

DxDy {
	Int dx, dy, tailTile, headTile;
}

Directions extends Enum<DxDy> {
	left: {
		dx: -1
		dy: 0
		headTile: 9
		tailTile: 13
	}
	right: {
		dx: 1
		dy: 0
		headTile: 10
		tailTile: 14
	}
	up: {
		dx: 0
		dy: -1
		headTile: 11
		tailTile: 15
	}
	down: {
		dx: 0
		dy: 1
		headTile: 12
		tailTile: 16
	}
}

Map<DxDy, Map<Int, DxDy>> tailDxDyFromTile = {
	left: {
		horizontal: left
		leftUp: up
		leftDown: down
	}
	right: {
		horizontal: right
		rightUp: up
		rightDown: down
	}
	up: {
		vertical: up
		rightDown: left
		leftDown: right
	}
	down: {
		vertical: down
		rightUp: left
		leftUp: right
	}
}

Map<DxDy, Map<DxDy, Int>> bodyTile = {
	left: {
		left: horizontal
		right: horizontal
		up: leftUp
		down: leftDown
	}
	right: {
		left: horizontal
		right: horizontal
		up: rightUp
		down: rightDown
	}
	up: {
		left: rightDown
		right: leftDown
		up: vertical
		down: vertical
	}
	down: {
		left: rightUp
		right: leftUp
		up: vertical
		down: vertical
	}
}

Map<Key, DxDy> keyDxDy = {
	A: left
	D: right
	W: up
	S: down
}

// options
Int score, headX, headY, tailX, tailY, tailDxDy, dxDy, oldDxDy

options = Window("Options") {
	start: Centered.Button("Start") {
		onClick() {
			options.close()
			
			global field = TileMap(width, height, tiles).fill(floor)
			for(x = 0 ..< width) field[x, 0] = field[x, height - 1] = wall
			for(y = 0 ..< height) field[0, y] = field[width - 1, y] = wall
			placeApple()
			
			global score = 0
			global headX = 3
			global headY = tailX = tailY = 2
			global tailDxDy = dxDy = oldDxDy = left
			
			game.open()
		}
	}
	object: Column() {
		Table(2) {
			Right.Text("Field width: ")
			TextField(#width, 10..100)
			Right.Text("Field height: ")
			TextField(#height, 10..100)
			Right.Text("Initial length: ")
			TextField(#food, 1..)
			Right.Text("Length increment: ")
			TextField(#appleFood, 1..)
			$start
		}
	}	
}

// main

placeApple() {
	do {
		appleX = Int.random(0 ..< width)
		appleY = Int.random(0 ..< height)
		if(field[appleX, appleY] == floor) {
			field[appleX, appleY] = apple
			break
		}
	}
}

game = Hidden.Window("Game") {
	canvas: MaxIntScaled.Canvas(field)
	object: Column {
		Center.Text("Score: \(score)")
		$canvas
	}
	logicPerSecond: 6
	onKeyDown(key) {
		dxDy ?= keyDxDy[key]
	}
	logic() {
		oldDxDy = dxDy
		field[x, y] = bodyTile[oldDxDy][dxDy]
		x += dxDy.dx
		y += dxDy.dy
		switch(field[x, y]) {
			case floor:
			case apple:
				food += appleFood
				score++
				placeApple()
			default:
				game.close()
				gameOver = NoButtons.Window("Game over!") {
					ok: Centered.Button("OK") {
						onClick() {
							gameOver.close()
							options.open()
						}
					}
					object: Column {
						Center.Text("You scored \score\ points.")
						$ok
					}
				}
		}
		field[x, y] = dxDy.headTile
		if(food > 0) {
			food--
		} else {
			field[tailX, tailY] = floor
			tailX += tailDxDy.dx
			tailY += tailDxDy.dy
			tailDxDy = tailDxDyFromTile[field[tailX, tailY]][tailDxDy]
		}
		field[tailX, tailY] = tailDxDy.tailTile
		global oldDxDy = dxDy
	}
}

