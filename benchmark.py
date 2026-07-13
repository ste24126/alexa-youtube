import timeit

setup = """
event = {'request': {'locale': 'en-US'}}
strings_fr = 1
strings_it = 2
strings_de = 3
strings_es = 4
strings_ja = 5
strings_en = 6

def original(event):
    if event['request']['locale'][0:2] == 'fr':
        strings = strings_fr
    elif event['request']['locale'][0:2] == 'it':
        strings = strings_it
    elif event['request']['locale'][0:2] == 'de':
        strings = strings_de
    elif event['request']['locale'][0:2] == 'es':
        strings = strings_es
    elif event['request']['locale'][0:2] == 'ja':
        strings = strings_ja
    else:
        strings = strings_en

def optimized(event):
    locale = event['request']['locale'][0:2]
    if locale == 'fr':
        strings = strings_fr
    elif locale == 'it':
        strings = strings_it
    elif locale == 'de':
        strings = strings_de
    elif locale == 'es':
        strings = strings_es
    elif locale == 'ja':
        strings = strings_ja
    else:
        strings = strings_en
"""

print("Original:", timeit.timeit("original(event)", setup=setup, number=1000000))
print("Optimized:", timeit.timeit("optimized(event)", setup=setup, number=1000000))
