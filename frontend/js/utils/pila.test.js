// Self-check del TAD Pila. Correr: node js/utils/pila.test.js
import { Pila } from './pila.js';

const p = new Pila();
console.assert(p.estaVacia() === true, 'pila nueva debe estar vacía');
console.assert(p.tamano() === 0, 'tamaño inicial 0');

p.push('a');
p.push('b');
console.assert(p.tamano() === 2, 'tamaño tras 2 push');
console.assert(p.peek() === 'b', 'peek devuelve el tope sin sacarlo');
console.assert(p.tamano() === 2, 'peek no muta');

console.assert(p.pop() === 'b', 'pop devuelve LIFO (b)');
console.assert(p.pop() === 'a', 'pop devuelve LIFO (a)');
console.assert(p.estaVacia() === true, 'vacía tras sacar todo');
console.assert(p.pop() === undefined, 'pop en vacía => undefined');

console.log('pila.test.js OK');
