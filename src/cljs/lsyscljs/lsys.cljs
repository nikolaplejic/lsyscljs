(ns lsyscljs.lsystems)

;; L-system info: https://en.wikipedia.org/wiki/L-system

(def line-length 5)

(defn rotate [ang]
  (let [rad (* ang (/ js/Math.PI 180))
        x (* line-length (js/Math.cos rad))
        y (* -1 line-length (js/Math.sin rad))]
    [x y]))

(defn get-description [lsys-type]
  (case lsys-type
    :koch {:start [:F]
           :rules {:F [:F :+ :F :- :F :- :F :+ :F]}
           :angle 90}
    :dragon {:start [:F :X]
             :rules {:X [:X :+ :Y :F]
                     :Y [:F :X :- :Y]}
             :angle 90}
    :sierpinski {:start [:A]
                 :rules {:A [:B :- :A :- :B]
                         :B [:A :+ :B :+ :A]}
                 :angle 60}))

(def cmds
  {:F :fw :A :fw :B :fw
   :+ :lt
   :- :rt
   :X :noop :Y :noop})

(defn lsys [start rules iters]
  (loop [state start
         remaining-iterations iters]
    (let [new-state (atom [])]
      (doseq [elem state
              :let [supstitution (or (elem rules) [elem])]]
        (swap! new-state into supstitution))
      (if (> remaining-iterations 0)
        (recur @new-state (dec remaining-iterations))
        @new-state))))

(defn draw-elem [ctx meaning pos ang rotation]
  (.moveTo ctx (first pos) (second pos))
  (case meaning
    :fw (if (= ang 0)
          (let [new-pos [(+ line-length (first pos)) (second pos)]]
            (.lineTo ctx (first new-pos) (second new-pos))
            (.stroke ctx)
            {:pos new-pos :ang ang})
          (let [rot (rotate ang)
                new-pos [(+ (first rot) (first pos)) (+ (second rot) (second pos))]]
            (.lineTo ctx (first new-pos) (second new-pos))
            (.stroke ctx)
            {:pos new-pos :ang ang}))
    :lt {:pos pos :ang (+ ang rotation)}
    :rt {:pos pos :ang (- ang rotation)}
    :noop {:pos pos :ang ang}))

(defn draw [ctx system angle]
  (let [current-pos (atom [900 800])
        current-ang (atom 0)]
    (doseq [elem system
            :let [meaning (elem cmds)
                  update (draw-elem ctx meaning @current-pos @current-ang angle)]]
      (js/console.log (pr-str elem meaning update))
      (reset! current-pos (:pos update))
      (reset! current-ang (:ang update)))))

(defn get-canvas-context []
  (let [canvas (js/document.getElementById "lsys")]
    (.getContext canvas "2d")))

(let [ctx (get-canvas-context)
      description (get-description :sierpinski)
      system (lsys (:start description) (:rules description) 5)]
  (draw ctx system (:angle description)))
