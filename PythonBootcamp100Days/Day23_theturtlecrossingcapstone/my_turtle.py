from turtle import Turtle

class My_turtle(Turtle):

    def __init__(self, position):
        super().__init__()
        self.shape('turtle')
        self.color('white')
        self.penup()
        self.goto(position)
        self.setheading(90)

    def move_forward(self):
        y = self.ycor() + 10
        self.goto(self.xcor(), y)



