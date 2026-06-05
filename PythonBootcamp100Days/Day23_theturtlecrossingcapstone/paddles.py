from turtle import Turtle
import random

class Paddles(Turtle):
    
    def __init__(self):
        super().__init__()
        self.shape('square')
        self.shapesize()

    def create_paddles(self):
        for index in range(0, 10):
            paddles = Paddles(shape = "square")
            paddles.penup()
            x = 200
            y = random.randint(0,200)
            paddles.goto(x, y)
