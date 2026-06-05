from menu import Menu
from coffee_maker import CoffeeMaker
from money_machine import MoneyMachine

my_menu = Menu()
maker = CoffeeMaker()
money = MoneyMachine()
while True:
    options = my_menu.get_items()
    choices = input(f"Your choices ({options}): ")
    if choices == "":
        break
    elif choices == 'report':
        maker.report()
        money.report()
    else:
        drinks = my_menu.find_drink(choices)
        if maker.is_resource_sufficient(drinks) and money.make_payment(drinks.cost):
            maker.make_coffee(drinks)
