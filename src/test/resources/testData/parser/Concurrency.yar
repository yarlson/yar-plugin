package main

fn square(v i32) i32 {
    return v * v
}

fn worker(jobs chan[i32], results chan[i32]) void {
    for true {
        job := chan_recv(jobs) or |err| {
            break
        }
        chan_send(results, job * job) or |err| {
            break
        }
    }
}

fn main() i32 {
    jobs := chan_new[i32](4)
    results := chan_new[i32](4)

    taskgroup []void {
        spawn fn() void {
            chan_send(jobs, 2) or |err| {
                return
            }
            chan_send(jobs, 3) or |err| {
                return
            }
            chan_close(jobs)
        }()

        spawn fn() void {
            worker(jobs, results)
            chan_close(results)
        }()
    }

    values := taskgroup []i32 {
        spawn square(chan_recv(results) or |err| {
            return 1
        })
        spawn square(chan_recv(results) or |err| {
            return 1
        })
    }

    first := values[0]
    second := values[1]
    print(to_str(first + second) + "\n")
    return 0
}
