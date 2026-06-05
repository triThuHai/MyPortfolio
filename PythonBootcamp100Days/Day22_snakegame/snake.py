from turtle import Turtle
import time

STARTING_POINTS = [(0, 0), (-20, 0), (-40, 0)]
STEP = 20
UP = 90
DOWN = 270
LEFT = 180
RIGHT = 0
x0 = 0

class Snake:

    def __init__(self):
        self.snakes = []
        self.create_snake()
        self.head = self.snakes[0]

    def create_snake(self):
        for position in STARTING_POINTS:
            self.add_segment(position)

    def add_segment(self, position):
        my_turtle = Turtle("square")
        my_turtle.color("white")
        my_turtle.penup()
        my_turtle.goto(position)
        self.snakes.append(my_turtle)

    def extend_snake(self):
        self.add_segment(self.snakes[-1].position())

    def move(self):
            for sequence in range(len(self.snakes) - 1, 0, -1):
                new_x = self.snakes[sequence - 1].xcor()
                new_y = self.snakes[sequence - 1].ycor()
                self.snakes[sequence].goto(new_x, new_y)
            self.head.forward(STEP)

    def right(self):
        if self.head.heading() != LEFT:
            self.head.setheading(RIGHT)

    def left(self):
        if self.head.heading() != RIGHT:
            self.head.setheading(LEFT)

    def up(self):
        if self.head.heading() != DOWN:
            self.head.setheading(UP)

    def down(self):
        if self.head.heading() != UP:
            self.head.setheading(DOWN)

