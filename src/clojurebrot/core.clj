(ns clojurebrot.core
  (:require [quil.core :as q]
            [quil.middleware :as m]))

(def WIDTH 1280)
(def HEIGHT 800)
(def ITERATIONS 50)

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
  (doseq [x (range (- 0 (/ WIDTH 2)) WIDTH)
          y (range (- 0 (/ HEIGHT 2)) HEIGHT)
          :let [zx (+ cx (* x scale))
                zy (+ cy (* y scale))]]
    (loop [a zx
           b zy
           n 1]
      (let [a' (- (* a a) (* b b) (- 0 zx))
            b' (+ (* 2 a b) zy)
            z2 (+ (* a' a') (* b' b'))]
        (cond
          (> n (dec ITERATIONS)) nil
          (> z2 limit) (let [smooth (+ (dec n) (- 1 (/ (Math/log (Math/log (Math/sqrt z2))) (Math/log 2))))]
                         (q/stroke (/ smooth ITERATIONS) 0.6 1)
                         (q/point (+ x (/ WIDTH 2)) (+ y (/ HEIGHT 2))))
          :else (recur a' b' (inc n)))))))

(defn -main
  [& args]
  (q/sketch
    :title "Mandelbrot fractal"
    :size [WIDTH HEIGHT]
    :setup setup
    :draw draw-state
    :middleware [m/fun-mode]))
