(ns clojurebrot.core
  (:require [quil.core :as q]
            [quil.middleware :as m])
  (:gen-class))

(def WIDTH 1280)
(def HEIGHT 800)
(def HALF_WIDTH (/ WIDTH 2))
(def HALF_HEIGHT (/ HEIGHT 2))
(def ITERATIONS 50)
(def LOG2 (Math/log 2))
(def coords
  (for [x (range WIDTH)
        y (range HEIGHT)]
    [x y]))

(defn setup []
  (q/frame-rate 5)
  (q/color-mode :hsb 1 1 1)
  {:cx         -0.5
   :cy         0
   :scale      0.0035
   :limit      4
   :saturation 0.6})

(defn zx-fn [cx x scale] (+ cx (* (- x HALF_WIDTH) scale)))
(defn zy-fn [cy y scale] (+ cy (* (- y HALF_HEIGHT) scale)))
(defn a'-fn [a b zx] (- (* a a) (* b b) (- 0 zx)))
(defn b'-fn [a b zy] (+ (* 2 a b) zy))
(defn z2-fn [a' b'] (+ (* a' a') (* b' b')))
(defn calculate-hue [n z2] (/ (+ n (- 1 (/ (Math/log (Math/log (Math/sqrt z2))) LOG2))) ITERATIONS))

(defn set-pixel
  [x y n z2 s b]
  (->> (q/color (calculate-hue n z2) s b)
       (q/set-pixel x y)))

(defn mandelbrot-iter
  [x y zx zy {:keys [limit saturation]}]
  (loop [a zx
         b zy
         n 1]
    (let [a' (a'-fn a b zx)
          b' (b'-fn a b zy)
          z2 (z2-fn a' b')]
      (cond
        (> n (dec ITERATIONS)) nil
        (> z2 limit) (set-pixel x y n z2 saturation 1)
        :else (recur a' b' (inc n))))))

(defn get-intermediates
  [x y {:keys [cx cy limit saturation scale] :as state}]
  (let [zx (zx-fn cx x scale)
        zy (zy-fn cy y scale)]
    (mandelbrot-iter x y zx zy state)))

(defn draw-state
  [{:keys [cx cy scale limit saturation] :as state}]
  (q/background 0)
  (doall (map (fn [[x y]] (get-intermediates x y state)) coords)))

(defn update-state [state]
  (assoc state :saturation (+ 0.6 (- 0.1 (rand 0.2)))))

(defn -main
  [& args]
  (q/sketch
    :title "Mandelbrot fractal"
    :size [WIDTH HEIGHT]
    :setup setup
    :draw #(time (draw-state %))
    :update update-state
    :middleware [m/fun-mode]
    :feature [:exit-on-close]
    :renderer :p2d))
