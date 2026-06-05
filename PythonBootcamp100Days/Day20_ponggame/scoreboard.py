from turtle import Turtle

ALIGNMENT = "center"
FONT = ("Courier", 24, "normal")

class Score(Turtle):

    def __init__(self):
        super().__init__()
        self.l_score = 0
        self.r_score = 0
        self.color("white")
        self.penup()
        self.update_score()
        self.hideturtle()

    def update_score(self):
        self.goto(100, 250)
        self.write(self.r_score, align = ALIGNMENT, font = FONT)
        self.goto(-100, 250)
        self.write(self.l_score, align=ALIGNMENT, font=FONT)
    
    def add_r_score(self):
        self.r_score += 1
        self.clear()
        self.update_score()

    def add_l_score(self):
        self.l_score += 1
        self.clear()
        self.update_score()
