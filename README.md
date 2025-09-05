# ARSW-Lab03
## Concurrent programming, race conditions, and thread synchronization.

**Colombian School of Engineering Julio Garavito**  
**Software Architectures - ARSW**  
**Laboratory Number 2**

**Members:**
- Juan Esteban Medina Rivas
- María Paula Sánchez Macías

---

## Part I - Producer/Consumer

> 1. Check the program's operation and run it. While this is happening, run jVisualVM and check the CPU consumption of the corresponding process. What is causing this consumption? Which class is responsible?

Consumption is due to the *Consumer* class because it is constantly checking whether the *queue* has an element to remove, performing unnecessary validations since the *Producer* has a wait time between each iteration in which it adds elements to the *queue*.

**Code execution and visualization in *jVisualVM***

<img src="img/1.1 visualvm-ARST.png">

> 2. Make the necessary adjustments so that the solution uses the CPU more efficiently, bearing in mind that, for now, production is slow and consumption is fast. Check with JVisualVM that CPU consumption is reduced.

As a first solution, we considered using *Thread.sleep(1000)* to delay the execution of *Consumer* and thus avoid unnecessary validations, but this solution, although it makes CPU usage more efficient, is still not the best solution as it increases latency. To address this, we are going to implement thread control with wait/notifyAll, which will make the implementation more efficient in terms of CPU usage without increasing latency.

**Code execution results**
<img src="img/1.2 wait-notifyall.png">

**Efficient code execution and visualization in *jVisualVM***

<img src="img/1.2 visualvm-efficient.png">

> 3. Now make the producer produce very quickly and the consumer consume slowly. Given that the producer knows a stock limit (how many items it should have, at most, in the queue), make sure that this limit is respected. Check the API of the collection used as a queue to see how to ensure that this limit is not exceeded. Verify that, when setting a small limit for ‘stock’, there is no high CPU consumption or errors.

For this requirement, we will use *BlockingQueue*, a class that correctly defines the use of multiple threads, blocking while being used by a thread and unblocking when the action is complete. It also allows you to set a limit on the number of elements that will go into the array, meeting the requirements of the exercise.

**Result of executing the code for a stock limit of 3**

<img src="img/1.3 BlockingQueue.png">

## Parte II. – Antes de terminar la clase.

Teniendo en cuenta los conceptos vistos de condición de carrera y sincronización, haga una nueva versión -más eficiente- del ejercicio anterior (el buscador de listas negras). En la versión actual, cada hilo se encarga de revisar el host en la totalidad del subconjunto de servidores que le corresponde, de manera que en conjunto se están explorando la totalidad de servidores. Teniendo esto en cuenta, haga que:

- La búsqueda distribuida se detenga (deje de buscar en las listas negras restantes) y retorne la respuesta apenas, en su conjunto, los hilos hayan detectado el número de ocurrencias requerido que determina si un host es confiable o no (_BLACK_LIST_ALARM_COUNT_).
- Lo anterior, garantizando que no se den condiciones de carrera.

## Part III - Immortals

**Context of the problem**

![](http://files.explosm.net/comics/Matt/Bummed-forever.png)

>1. This is a game in which:
There are N immortal players.
Each player knows the other N−1 players.
Each player continuously attacks another immortal. The one who attacks first subtracts M health points from their opponent and adds the same amount to their own health.
The game may never have a single winner. Most likely, in the end, only two players will remain, fighting endlessly by gaining and losing health points.

>2.1 How was the game's functionality implemented in the original code?

The functionality was implemented as follows:

- Each immortal is a thread (extends `Thread`) that runs an infinite loop in the `run()` method.

- In each iteration, the immortal randomly selects an opponent from the `immortalsPopulation` list, making sure not to attack itself.

- The `fight()` method implements the combat mechanics: if the opponent has `health > 0`, it subtracts `defaultDamageValue` (10 points) from the opponent and adds the same amount to its own health.

```java
if (i2.getHealth() > 0) {
    i2.changeHealth(i2.getHealth() - defaultDamageValue);
    this.health += defaultDamageValue;
}
```
>2.2 Given the intention of the game, what should be the total sum of all players' health points for N players?

The value should be N × 100, where N is the number of players. This is because each immortal starts with DEFAULT_IMMORTAL_HEALTH = 100 health points, and during fights, the health points one immortal loses are exactly the same as those gained by another. For example: For 3 immortals, the total would be 300 health points.

>3.1 How does the 'pause and check' option work?

Tests

<img src="img/part3 HealthInconcurrent.png">

>3.1 Is the invariant satisfied?

No, each time the button is clicked, the total health changes completely and does not maintain the consistency it should.

>4. Do whatever is necessary so that before printing the current results, all other threads are paused. Additionally, implement the ‘resume’ option.

**Pause and Check Buttom**

<img src="img/part3 PauseButtom.png">

**Resume Buttom**

<img src="img/part3 ResumeButtom.png">

5. Check the functionality again. Is the invariant satisfied or not?

Yes, the invariant satisfied the conditions

New Test

<img src="img/part3 HealthPerfect1.png">

<img src="img/part3 HealthPerfect2.png">

<img src="img/part3 HealthPerfect3.png">

>6 Identify possible critical regions concerning the fight between the immortals and implement a locking strategy.

Identified Critical Regions:

**Main Critical Region – Health Transfer:**

```java
    i2.changeHealth(i2.getHealth() - defaultDamageValue);
    this.health += defaultDamageValue;
    updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
```
**changeHealth() Method:**

```java
public void changeHealth(int v) {
    health = v;
}
```

**Locking Strategy Used:**

- A single synchronization object (`syncObject`) shared among all immortals.

- All fights are synchronized using this single object.

- This completely serializes the fights.

- By using a single synchronization object, it avoids deadlocks between multiple locks.


> 7. After implementing your strategy, run your program and pay attention to whether it stops. If it does, use the jps and jstack programs to identify why the program stopped.

By using a strategy with a single object for synchronization, we avoid any kind of problem, and the program did not freeze at any point.


> 9. Once the problem has been corrected, verify that the program continues to function consistently when 100, 1,000, or 10,000 immortals are executed. If, in these large cases, the invariant begins to fail again, you must analyze what was done in step 4.

**Testing 10 Inmortals**

<img src="img/3.9 10Inmortals.png">

**Testing 100 Inmortals**

<img src="img/3.9 100Inmortals.png">

**Testing 1000 Inmortals**

<img src="img/3.9 1000Inmortals.png">

**Testing 10000 Inmortals**

<img src="img/3.9 10000Inmortals.png">

> 10. One annoying element of the simulation is that at a certain point there are few living ‘immortals’ left, fighting futile battles with ‘immortals’ who are already dead. It is necessary to remove the dead immortals from the simulation as they die. To do this:
* Analyzing the simulation's operating scheme, could this create a race condition? Implement the functionality, run the simulation, and observe what problem arises when there are many ‘immortals’ in it.
* Correct the above problem __WITHOUT using synchronization__, as making access to the shared list of immortals sequential would make the simulation extremely slow.

To do this, we created a new flag *isDead* which is updated when the immortal's life is equal to or less than 0, which means that it is no longer counted as a combat option, thus avoiding failed battles.

> 11. Finally, implement the STOP option.

We make all the threads to be dead with *killinmortal* function so the STOP button works correctly and doesn't allow to resume the execution.

**Testing Stop Button**

<img src="img/3.11 stopButton.png">

<!--
### Criterios de evaluación

1. Parte I.
	* Funcional: La simulación de producción/consumidor se ejecuta eficientemente (sin esperas activas).

2. Parte II. (Retomando el laboratorio 1)
	* Se modificó el ejercicio anterior para que los hilos llevaran conjuntamente (compartido) el número de ocurrencias encontradas, y se finalizaran y retornaran el valor en cuanto dicho número de ocurrencias fuera el esperado.
	* Se garantiza que no se den condiciones de carrera modificando el acceso concurrente al valor compartido (número de ocurrencias).


2. Parte III.
	* Diseño:
		- Coordinación de hilos:
			* Para pausar la pelea, se debe lograr que el hilo principal induzca a los otros a que se suspendan a sí mismos. Se debe también tener en cuenta que sólo se debe mostrar la sumatoria de los puntos de vida cuando se asegure que todos los hilos han sido suspendidos.
			* Si para lo anterior se recorre a todo el conjunto de hilos para ver su estado, se evalúa como R, por ser muy ineficiente.
			* Si para lo anterior los hilos manipulan un contador concurrentemente, pero lo hacen sin tener en cuenta que el incremento de un contador no es una operación atómica -es decir, que puede causar una condición de carrera- , se evalúa como R. En este caso se debería sincronizar el acceso, o usar tipos atómicos como AtomicInteger).

		- Consistencia ante la concurrencia
			* Para garantizar la consistencia en la pelea entre dos inmortales, se debe sincronizar el acceso a cualquier otra pelea que involucre a uno, al otro, o a los dos simultáneamente:
			* En los bloques anidados de sincronización requeridos para lo anterior, se debe garantizar que si los mismos locks son usados en dos peleas simultánemante, éstos será usados en el mismo orden para evitar deadlocks.
			* En caso de sincronizar el acceso a la pelea con un LOCK común, se evaluará como M, pues esto hace secuencial todas las peleas.
			* La lista de inmortales debe reducirse en la medida que éstos mueran, pero esta operación debe realizarse SIN sincronización, sino haciendo uso de una colección concurrente (no bloqueante).

	

	* Funcionalidad:
		* Se cumple con el invariante al usar la aplicación con 10, 100 o 1000 hilos.
		* La aplicación puede reanudar y finalizar(stop) su ejecución.
		
		-->

<a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-nc/4.0/88x31.png" /></a><br />Este contenido hace parte del curso Arquitecturas de Software del programa de Ingeniería de Sistemas de la Escuela Colombiana de Ingeniería, y está licenciado como <a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/">Creative Commons Attribution-NonCommercial 4.0 International License</a>.
