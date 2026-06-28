// TAD Pila (LIFO) — estructura de datos del sílabo (RF-DS-03).
// Implementación propia; respaldo array interno encapsulado.
export class Pila {
  #items = [];

  push(elemento) {
    this.#items.push(elemento);
  }

  pop() {
    return this.#items.pop();
  }

  peek() {
    return this.#items[this.#items.length - 1];
  }

  estaVacia() {
    return this.#items.length === 0;
  }

  tamano() {
    return this.#items.length;
  }
}
