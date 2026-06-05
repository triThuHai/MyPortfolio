from turtle import Turtle, Screen
from paddles import Paddles
from ball import Ball
from scoreboard import Score
import time

screen = Screen()
screen.setup(width = 800, height = 600)
screen.bgcolor("black")
screen.title("Pong Game")
screen.tracer(0)

r_paddle = Paddles((350, 0))
l_paddle = Paddles((-350, 0))
ball = Ball()
score = Score()

screen.listen()
screen.onkey(r_paddle.up, "Up")
screen.onkey(r_paddle.down, "Down")
screen.onkey(l_paddle.up, "w")
screen.onkey(l_paddle.down, "s")

game_on = True
while game_on:
    screen.update()
    time.sleep(ball.move_speed)
    ball.move()
    #Collision to the wall
    if ball.ycor() > 280 or ball.ycor() < -280:
        ball.bouce_y()
    #Collision to paddles
    if (ball.distance(r_paddle) < 50 and ball.xcor() > 320) or (ball.distance(l_paddle) < 50 and ball.xcor() < -320):
        ball.bouce_x()
    #Right side missing
    if ball.xcor() > 380:
        ball.reset_position()
        score.add_l_score()
    #Left side missing
    if ball.xcor() < -380:
        ball.reset_position()
        score.add_r_score()

screen.exitonclick()
