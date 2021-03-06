load("resources.ees")

Sprite {
	Position position
	setPosition($position) {
		$x = $pivot[$position].x
		$y = $pivot[$position].y
	}
}

showMessage("Вам надо перевезти волка, козу и капусту на другой берег. В лодку можно брать только одного 
		пассажира. Волка нельзя оставлять с козой, а козу - с капустой.")

gameOver(String message) {
	showMessage(message)
	loadObject("resources.ees")
}
	
Window(scene) {
  onClick(x, y) {
		scene.onCollision(x, y, (sprite) {
			if(sprite == boat) {
				boat.setPosition(boat.position == left ? right : left)
				spriteInBoat.setPosition(inBoat)
			} else if(sprite.position == inBoat || sprite.position == boat.position && spriteInBoat == null) {
				sprite.setPosition(sprite.position == inBoat ? boat.position : inBoat)
				spriteInBoat = sprite.position == inBoat ? sprite : null;
			}
			
			if(wolf.position == right && goat.position == right && cabbage.position == right) {
				gameOver("У вас получилось!")
			} else if(goat.position != inBoat && goat.position != boat.position) {
				if(cabbage.position == goat.position) {
					gameOver("Коза съела капусту!")
				} else if(wolf.position == goat.position) {
					gameOver("Волк съел козу!")
				}
			}
		})
	}
}