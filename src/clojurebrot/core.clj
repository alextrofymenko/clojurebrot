(ns clojurebrot.core
  (:require [quil.core :as q]
            [quil.middleware :as m]))

(def WIDTH 1280)
(def HEIGHT 800)
(def HALF_WIDTH (/ WIDTH 2))
(def HALF_HEIGHT (/ HEIGHT 2))
(def ITERATIONS 50)
(def LOG2 (Math/log 2))

(defn setup []
  (q/frame-rate 5)
  (q/color-mode :hsb 1 1 1)
  {:cx    -0.5
   :cy    0
   :scale 0.0035
   :limit 4})

(defn draw-state
  [{:keys [cx cy scale limit]}]
  (q/background 0)
  (doseq [x (range WIDTH)
          y (range HEIGHT)
          :let [zx (+ cx (* (- x HALF_WIDTH) scale))
                zy (+ cy (* (- y HALF_HEIGHT) scale))]]
    (loop [a zx
           b zy
           n 1]
      (let [a' (- (* a a) (* b b) (- 0 zx))
            b' (+ (* 2 a b) zy)
            z2 (+ (* a' a') (* b' b'))]
        (cond
          (> n (dec ITERATIONS)) nil
          (> z2 limit) (let [smooth (+ n (- 1 (/ (Math/log (Math/log (Math/sqrt z2))) LOG2)))]
                         (q/stroke (/ smooth ITERATIONS) 0.6 1)
                         (q/point x y))
          :else (recur a' b' (inc n)))))))

(defn -main
  [& args]
  (set! *warn-on-reflection* true)
  (set! *unchecked-math* true)
  (q/sketch
    :title "Mandelbrot fractal"
    :size [WIDTH HEIGHT]
    :setup setup
    :draw #(println (time (draw-state %)))
    :middleware [m/fun-mode]
    :feature [:exit-on-close]
    :renderer :p2d))
