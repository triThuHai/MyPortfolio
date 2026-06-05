from turtle import Turtle, Screen
from my_turtle import My_turtle
import time
import random

screen = Screen()
screen.setup(width = 600, height = 600)
screen.bgcolor("black")
screen.title("Crossing Capstone")
screen.tracer(0)
paddles_list = []

turtle = My_turtle((0, -280))

screen.listen()
screen.onkey(turtle.move_forward, "Up")

for index in range(0, 10):
    paddles = Turtle(shape = "square")
    paddles.color('white')
    w = random.uniform(0.5, 5)
    h =random.uniform(0.5, 3)
    paddles.shapesize(h, w)
    paddles.penup()
    x = random.randint(280, 600)
    y = random.randint(-200, 200)
    paddles.goto(x,y)
    paddles_list.append(paddles)

game_on = True
while game_on:
    screen.update()
    time.sleep(0.1)
    for paddle in paddles_list:
        paddle.setheading(180)
        paddle.forward(10)



screen.exitonclick()

