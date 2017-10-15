from setuptools import setup

setup(
   name='hackhealthcare-personalized',
   version='0.1',
   description='A useful module',
   author='Steven Pease',
   author_email='peasteven@gmail.com',
   packages=['hackhealthcare-personalized'],  #same as name
   install_requires=['aiohttp', 'aiohttp-index'], #external packages as dependencies
)
